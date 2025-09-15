# **{2_9팀}**

[https://www.notion.so/1-26719446708e803a8a05c25e42ecf0bc](https://www.notion.so/1-26719446708e803a8a05c25e42ecf0bc?pvs=21)

## **팀원 구성**

정기주 (https://github.com/jeonggiju)
권지인 (https://github.com/kjn4101)
김수연 (https://github.com/sooyeonz)
김찬혁 (https://github.com/chanhyeok0201)

---

## **프로젝트 소개**

- 국내 주요 지수(KRX300, KOSPI, KOSDAQ 등)의 **지수 정보/일별 데이터**를 수집·저장·분석
- 금융 지수 데이터를 효과적으로 관리하고 분석할 수 있도록 돕는 웹 사이트
- 프로젝트 기간: 2024.09.05 ~ 2024.09.16

---

## **기술 스택**

- Backend: Java 17, Spring Boot 3, Spring Data JPA
- Database: PostgreSQL
- 공통 Tool: Git & Github, Discord, Notion
- Open API: 공공데이터포털

---

## **팀원별 구현 기능 상세**

### 정기주

<img width="1107" height="729" alt="image" src="https://github.com/user-attachments/assets/5677fec2-9673-45a8-bde3-655607b21533" />

- **지수 정보 관리 API**
    - 지수 메타정보 생성/조회/검색
    - 인덱스명/분류/상태 기반 필터 & 정렬

### 권지인

<img width="1101" height="732" alt="image" src="https://github.com/user-attachments/assets/045f1028-637e-442a-8fa0-0a980e8b76fe" />

<img width="1101" height="950" alt="image" src="https://github.com/user-attachments/assets/a64f1e3e-0545-49a9-a556-e117fe691d17" />

- **지수 데이터 관리 API**
    - 메인 페이지에서 전체 지수 데이터를 무한 스크롤 기반 커서 페이지네이션으로 조회 및 필터링 검색
    - 사용자가 직접 지수 데이터 생성 가능
    - 대시보드 상에서 지수별 차트 데이터 조회 및 지수의 기간별 성과 TOP10 조회
    - 인덱스명/분류/상태 기반 필터링 & 정렬
- **연동 작업 API**
    - 연동 이력 목록을 무한 스크롤 기반 커서 페이지네이션으로 조회 및 필터링 검색

### 김수연

<img width="1101" height="732" alt="image" src="https://github.com/user-attachments/assets/c0075011-e86f-447c-a6bf-51c2d7ca86e1" />

<img width="1101" height="950" alt="image" src="https://github.com/user-attachments/assets/0b31ba34-ba54-451e-9fc9-c6cd0aca7a97" />

- **지수 데이터 관리 API**
    - 지수 데이터 삭제/수정
    - 관심 지수 성과 조회
    - 지수 데이터 CSV export (바이트 스트림 사용)
- **연동 작업 API**
    - 지수 정보 연동
    - 지수 데이터 연동
- **Open API parsing**
    - 성능 향상을 위해 배치 처리와 복합 조건으로 DB 조회

### 김찬혁

<img width="1101" height="950" alt="image" src="https://github.com/user-attachments/assets/30b3355d-e4f1-4634-adc7-5608ef9fd2be" />

- **자동 연동 설정 API**
    - 지수별 enabled 토글로 자동 동기화 대상 관리
    - 매일 06:00 KST 스케줄러가 enabled=true인 지수만 동기화
    - **JPA Specification** 기반 필터( indexInfoIdEq / enabledEq / idAfter ) & **커서 페이지네이션**

---

## **파일 구조**

```

 src
   ├─ main
   │  ├─ java
   │  │  └─ com
   │  │     └─ codeit
   │  │        └─ findex
   │  │           ├─ FindexApplication.java
   │  │           ├─ common
   │  │           │  ├─ config
   │  │           │  │  └─ SwaggerConfig.java
   │  │           │  ├─ exception
   │  │           │  │  └─ GlobalExceptionHandler.java
   │  │           │  ├─ openapi
   │  │           │  │  ├─ controller
   │  │           │  │  │  └─ ApiDataInitializer.java
   │  │           │  │  ├─ dto
   │  │           │  │  │  └─ ApiResponseDto.java
   │  │           │  │  └─ service
   │  │           │  │     ├─ ApiIndexDataService.java
   │  │           │  │     └─ ApiIndexInfoService.java
   │  │           │  └─ util
   │  │           │     └─ ApiLoggingAspect.java
   │  │           ├─ indexData
   │  │           │  ├─ controller
   │  │           │  │  ├─ IndexChartController.java
   │  │           │  │  ├─ IndexDataController.java
   │  │           │  │  ├─ IndexDataCsvExportController.java
   │  │           │  │  ├─ IndexDataQueryController.java
   │  │           │  │  ├─ IndexFavoriteController.java
   │  │           │  │  └─ IndexPerformanceController.java
   │  │           │  ├─ domain
   │  │           │  │  ├─ IndexData.java
   │  │           │  │  ├─ PerformancePeriodType.java
   │  │           │  │  └─ PeriodType.java
   │  │           │  ├─ dto
   │  │           │  │  ├─ IndexChartResponse.java
   │  │           │  │  ├─ IndexDataRequestDto.java
   │  │           │  │  ├─ IndexDataResponseDto.java
   │  │           │  │  ├─ IndexDataUpdateRequest.java
   │  │           │  │  ├─ IndexDataUpdateResponse.java
   │  │           │  │  ├─ IndexFavoritePerformance.java
   │  │           │  │  ├─ IndexPerformanceDto.java
   │  │           │  │  └─ IndexPerformanceRankResponse.java
   │  │           │  ├─ repository
   │  │           │  │  └─ IndexDataRepository.java
   │  │           │  └─ service
   │  │           │     ├─ IndexChartService.java
   │  │           │     ├─ IndexDataCsvExportService.java
   │  │           │     ├─ IndexDataQueryService.java
   │  │           │     ├─ IndexDataService.java
   │  │           │     ├─ IndexFavoriteService.java
   │  │           │     └─ IndexPerformanceService.java
   │  │           ├─ indexInfo
   │  │           │  ├─ controller
   │  │           │  │  └─ IndexInfoController.java
   │  │           │  ├─ domain
   │  │           │  │  └─ IndexInfo.java
   │  │           │  ├─ dto
   │  │           │  │  ├─ request
   │  │           │  │  │  ├─ IndexInfoCreateRequestDto.java
   │  │           │  │  │  ├─ IndexInfoGetRequestDto.java
   │  │           │  │  │  └─ IndexInfoUpdateRequestDto.java
   │  │           │  │  └─ response
   │  │           │  │     ├─ IndexInfoCreateResponseDto.java
   │  │           │  │     ├─ IndexInfoGetByIdResponseDto.java
   │  │           │  │     ├─ IndexInfoGetResponseDto.java
   │  │           │  │     ├─ IndexInfoSummaryResponseDto.java
   │  │           │  │     └─ IndexInfoUpdateResponseDto.java
   │  │           │  ├─ mapper
   │  │           │  │  └─ IndexInfoMapper.java
   │  │           │  ├─ repository
   │  │           │  │  └─ IndexInfoRepository.java
   │  │           │  └─ service
   │  │           │     └─ IndexInfoService.java
   │  │           └─ openApi
   │  │              ├─ controller
   │  │              │  ├─ AutoSyncConfigController.java
   │  │              │  └─ SyncJobController.java
   │  │              ├─ domain
   │  │              │  ├─ AutoSyncConfig.java
   │  │              │  └─ SyncJob.java
   │  │              ├─ dto
   │  │              │  ├─ request
   │  │              │  │  ├─ AutoSyncConfigUpdateRequest.java
   │  │              │  │  ├─ AutoSyncConfigDto.java
   │  │              │  │  ├─ IndexDataSyncRequest.java
   │  │              │  │  └─ SyncJobListRequest.java
   │  │              │  └─ response
   │  │              │     ├─ CursorPageResponseAutoSyncConfigDto.java
   │  │              │     ├─ PagedSyncJobResponse.java
   │  │              │     └─ SyncJobResponse.java
   │  │              ├─ mapper
   │  │              │  └─ AutoSyncConfigMapper.java
   │  │              ├─ repository
   │  │              │  ├─ AutoSyncConfigRepository.java
   │  │              │  ├─ DbAdvisoryLockRepository.java
   │  │              │  └─ SyncJobRepository.java
   │  │              ├─ scheduler
   │  │              │  └─ DailyEnabledIndexSyncScheduler.java
   │  │              ├─ service
   │  │              │  ├─ AutoSyncConfigService.java
   │  │              │  ├─ AutoSyncService.java
   │  │              │  ├─ IndexDataSyncService.java
   │  │              │  ├─ SyncJobQueryService.java
   │  │              │  └─ SyncService.java
   │  │              └─ spec
   │  │                 ├─ AutoSyncConfigBackfillRunner.java
   │  │                 ├─ AutoSyncConfigSpecs.java
   │  │                 └─ CursorUtil.java
   │  └─ resources
   │     ├─ application.yml
   │     ├─ application-local.yml
   │     ├─ application-localtest.yml
   │     ├─ application-prod.yml
   │     ├─ sql
   │     │  ├─ Findex.sql
   │     │  └─ dummy.sql
   │     └─ static
   │        ├─ favicon.ico
   │        ├─ index.html
   │        └─ assets
   │           ├─ index-CGZC7fCi.js
   │           └─ index-Dtn62Xmo.css
   └─ test
      └─ java
         └─ com
            └─ codeit
               └─ findex
                  ├─ FindexApplicationTests.java
                  ├─ indexData
                  │  └─ IndexInfoServiceTest.java
                  └─ openApi
                     └─ controller
                        └─ AutoSyncConfigControllerTest.java

```

---

## **구현 홈페이지**

https://findex-backend-production.up.railway.app/#/index-data

---

## **프로젝트 회고록**

- 시연 영상

https://drive.google.com/file/d/1ume5g5NSc2rE3w9FmRfSd4_gcy2fIbk2/view?usp=sharing

- PPT

https://www.canva.com/design/DAGyuWi1Aig/-B1ffzPpD4Wy5oSzuRq03g/edit
