package com.codeit.findex.openApi.spec;

import org.springframework.data.domain.Sort;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 커서 인코딩/디코딩 유틸 (URL-safe Base64)
 * 포맷: "sf=<sortField>;sd=<ASC|DESC>;sv=<escapedSortValue>;id=<lastId>;ff=<escapedFilter>"
 *   - sf: 정렬 필드 ("indexInfo.indexName" | "enabled" | "id")
 *   - sd: 정렬 방향
 *   - sv: 정렬값(문자열) — 세미콜론/등호/공백/퍼센트는 퍼센트 이스케이프
 *   - id: 마지막 id (tie-breaker)
 *   - ff: 필터 핑거프린트. 예) "en:true|ii:123" (enabled/indexInfoId)
 */
public final class CursorUtil {

	private CursorUtil() {}

	// 퍼센트 이스케이프
	private static String esc(String s) {
		if (s == null) return "";
		StringBuilder b = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (c == ';' || c == '=' || c == '%' || c == ' ' || c == '|') {
				b.append('%').append(Integer.toHexString(c));
			} else {
				b.append(c);
			}
		}
		return b.toString();
	}

	private static String unesc(String s) {
		if (s == null) return null;
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '%' && i + 2 < s.length()) {
				String hex = s.substring(i + 1, i + 3);
				try {
					int code = Integer.parseInt(hex, 16);
					b.append((char) code);
					i += 2;
				} catch (NumberFormatException ignore) {
					b.append(c);
				}
			} else {
				b.append(c);
			}
		}
		return b.toString();
	}

	public static String encode(String sortField, Sort.Direction dir, String sortValue, Long lastId,
		Boolean enabledFilter, Long indexInfoIdFilter) {
		Map<String, String> kv = new HashMap<>();
		kv.put("sf", sortField == null ? "id" : sortField);
		kv.put("sd", dir == null ? Sort.Direction.ASC.name() : dir.name());
		kv.put("sv", esc(sortValue));
		kv.put("id", lastId == null ? null : String.valueOf(lastId));

		// 필터 핑거프린트
		StringBuilder ff = new StringBuilder();
		if (enabledFilter != null) {
			if (ff.length() > 0) ff.append('|');
			ff.append("en:").append(enabledFilter);
		}
		if (indexInfoIdFilter != null) {
			if (ff.length() > 0) ff.append('|');
			ff.append("ii:").append(indexInfoIdFilter);
		}
		kv.put("ff", esc(ff.toString()));

		StringBuilder raw = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> e : kv.entrySet()) {
			if (!first) raw.append(';');
			first = false;
			raw.append(e.getKey()).append('=');
			if (e.getValue() != null) raw.append(e.getValue());
		}
		byte[] bytes = raw.toString().getBytes(StandardCharsets.UTF_8);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static Decoded decode(String cursor) {
		if (cursor == null || cursor.isBlank()) return null;
		try {
			String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
			String[] parts = raw.split(";");
			Map<String, String> kv = new HashMap<>();
			for (String p : parts) {
				int i = p.indexOf('=');
				if (i <= 0) continue;
				kv.put(p.substring(0, i), p.substring(i + 1));
			}
			String sf = kv.get("sf");
			String sd = kv.get("sd");
			String sv = unesc(kv.get("sv"));
			String id = kv.get("id");
			String ff = unesc(kv.get("ff")); // 없을 수 있음(하위호환)

			Sort.Direction dir = "DESC".equalsIgnoreCase(sd) ? Sort.Direction.DESC : Sort.Direction.ASC;
			Long lastId = (id == null || id.isBlank()) ? null : Long.valueOf(id);

			// ff 파싱
			Boolean en = null;
			Long ii = null;
			if (ff != null && !ff.isBlank()) {
				String[] toks = ff.split("\\|");
				for (String t : toks) {
					int j = t.indexOf(':');
					if (j > 0) {
						String k = t.substring(0, j);
						String v = t.substring(j + 1);
						if ("en".equals(k)) en = Boolean.valueOf(v);
						else if ("ii".equals(k)) ii = v.isBlank() ? null : Long.valueOf(v);
					}
				}
			}

			return new Decoded(true, sf, dir, sv, lastId, en, ii);
		} catch (Exception e) {
			return Decoded.invalid();
		}
	}

	public static final class Decoded {
		public final boolean valid;
		public final String sortField;
		public final Sort.Direction direction;
		public final String sortValue; // indexName 또는 "true"/"false" 또는 null
		public final Long lastId;
		public final Boolean enabledFilter;   // 커서 생성 시점의 enabled 필터
		public final Long indexInfoIdFilter;  // 커서 생성 시점의 indexInfoId 필터

		public Decoded(boolean valid, String sortField, Sort.Direction direction, String sortValue, Long lastId,
			Boolean enabledFilter, Long indexInfoIdFilter) {
			this.valid = valid;
			this.sortField = sortField;
			this.direction = direction;
			this.sortValue = sortValue;
			this.lastId = lastId;
			this.enabledFilter = enabledFilter;
			this.indexInfoIdFilter = indexInfoIdFilter;
		}
		public static Decoded invalid() {
			return new Decoded(false, null, Sort.Direction.ASC, null, null, null, null);
		}
	}

	/** idAfter만 받은 경우의 편의 생성기(정렬필드 id 기준) — ff는 넣지 않음(초기 커서) */
	public static Decoded ofIdOnly(String sortField, Sort.Direction direction, Long idAfter) {
		if (idAfter == null) return null;
		return new Decoded(true, sortField, direction, null, idAfter, null, null);
	}
}
