package com.codeit.findex.openApi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AutoSyncConfigUpdateRequest {

	@NotNull
	private Boolean enabled;

}
