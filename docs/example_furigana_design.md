# 예문 후리가나 설계 (파이프라인 사전 계산)

승인일: 2026-07-24

## 목표

세 예문 표시 화면(학습 카드 · 오늘의 예문 · 단어 상세)의 일본어 예문에 후리가나를 **항상 표시**한다.
후리가나는 런타임 사전 매칭이 아니라 **업로드 파이프라인에서 사전 계산**해 데이터에 포함한다.

## 배경 (대안 검토 결과)

- 예문 자체 토큰만으로는 문장 내 한자의 45.7%만 커버 (前·後·中·彼·日 등 비표제어 누락).
- 전역 사전(readings/all 방식)은 문자 커버리지 99.8%지만, 같은 표기에 발음이 2개 이상인
  표제어가 207개(今日·行く·一日·四 등)라 문맥 발음 오류를 구조적으로 피할 수 없음.
- 예문이 11,090개 고정 큐레이션 코퍼스 + 오프라인 우선 앱이므로, 파이프라인 사전 계산이
  정확도(검수 가능)·크기·플랫폼 일관성 모두에서 우세. 온디바이스 형태소 분석(플랫폼 비대칭,
  APK +11MB)과 런타임 외부 API(오프라인 설계 충돌)는 탈락.

## 1. 생성 파이프라인 (신규 `scripts/furigana/`)

- `generate_furigana.py`: fugashi(MeCab) + 풀 UniDic으로 예문을 형태소 분석해
  `(text, reading)` 세그먼트 목록으로 변환.
  - reading은 한자 포함 구간에만 붙는다. 오쿠리가나·가나·구두점은 본문 그대로 (조판 표준 루비).
  - 오쿠리가나 정렬: 형태소 표면형과 요미의 공통 가나 접두/접미를 제거해 한자 구간에 요미를 정렬.
  - `*단어*` 하이라이트 마커는 분석 전에 제거하고 세그먼트의 하이라이트 플래그로 보존.
- 정확도 보정 2중 장치:
  1. **큐레이션 토큰 우선** — 해당 예문 귀속 토큰 발음(vocab_pron)이 MeCab 결과와 다르면 토큰 채택.
  2. **오버라이드 파일** — `furigana_overrides.tsv` (예문 키, 구간, 교정 발음). 생성기 마지막에 적용.
     검수 교정이 여기 쌓여 재생성해도 유실되지 않는다.
- 산출물 `example_furigana.tsv`는 커밋 대상. upload.py는 MeCab 환경 없이 이 파일만 읽는다
  (생성과 업로드 분리).

## 2. 검수 (Sonnet 서브에이전트)

- 자동 플래깅: 다의 요미 표제어(207개) 등장 예문, 숫자+조수사, MeCab 미지어,
  토큰-MeCab 발음 불일치.
- Sonnet 에이전트가 100문장 단위 배치로 전수 검수(~110회). 오류 보고 → overrides 반영 →
  재생성 → 플래그 건 재확인. Fable(메인 세션)은 오케스트레이션만.

## 3. 데이터 스키마

- **Proto** (`ExampleChunk.proto`): `ChunkExample`에 새 번호로 추가.
  ```proto
  message ChunkExample {
      string japanese = 1;
      string korean = 2;
      repeated ChunkToken tokens = 3;
      repeated ChunkRuby furigana = 4;   // 신규
  }
  message ChunkRuby {
      string text = 1;
      string reading = 2;   // 비어 있으면 루비 없는 구간
  }
  ```
  하이라이트 구간은 별도 플래그 없이 기존 `japanese`의 `*` 마커 위치로 복원한다.
  생성기가 두 가지를 보장한다: (1) 세그먼트 text 연결 = 마커 제거 문장,
  (2) 마커 경계에서 세그먼트를 분할해 한 세그먼트가 하이라이트 안팎에 걸치지 않음.
  규칙 준수: `example_blob.py` 인코더 + `ExampleChunkCodecTest` 골든 blob 동시 갱신,
  필드 번호 불변·신규 번호만 추가. `readings/all`·토큰 표시는 기존 그대로 유지.
- **Room**: `example_sentences`에 `furigana` TEXT 컬럼 1개 (기본값 ""). `tokens` 컬럼과 동일한
  단일 컬럼 직렬화 패턴의 `FuriganaCodec` 신규. **DB v2 유지, MIGRATION_1_2에 통합,
  2.json 재생성** (프로젝트 DB 버전 정책).
- **Domain**: `Example`에 `furigana: List<RubySegment>` 추가.
  `RubySegment(text, reading, isHighlight)`.

## 4. UI 렌더링

- 공통 `RubyText` 컴포저블 신규 (Compose에 루비 없음): 세그먼트 단위 줄바꿈(FlowRow 계열),
  한자 구간 위에 작은 가나, 하이라이트 세그먼트는 기존 빨강 강조 유지.
  `buildHighlightedExample`의 예문 표시 역할을 대체.
- 적용: WordPageCardItem · TodayExampleSection · WordDetailScreen, 항상 표시.
- 폴백: furigana 세그먼트가 비어 있으면(동기화 전 구데이터) 기존 하이라이트 텍스트 그대로.

## 5. 반영 절차

1. 생성 + Sonnet 검수 완료 → `example_furigana.tsv` 확정
2. `upload.py --dry-run` 통계 확인 → **검수 결과를 사용자에게 보고한 뒤** Firestore 업로드
   (`examples_n{1-5}` blob에 furigana 포함, `content_update_date` 갱신 → 기존 사용자 증분 동기화)
3. 번들 스냅샷 DB 재생성 (`:data-kmp:testAndroidHostTest --tests "*PrepopulatedDbGenerator*"
   -PwriteSnapshot=true`) → Android assets + iOS 리소스 산출물 커밋

## 결정 사항 요약

| 항목 | 결정 |
|---|---|
| 계산 위치 | 파이프라인 사전 계산 (런타임 매칭 없음) |
| 사전 | 풀 UniDic (맥북 로컬 생성, 크기 제약 없음) |
| 표시 UX | 항상 표시 (토글 없음) |
| 루비 정렬 | 한자 구간 위에만 가나 (오쿠리가나는 본문) |
| 검수 | Sonnet 서브에이전트 전수 검수, Fable은 오케스트레이션만 |
| DB 버전 | v2 유지, MIGRATION_1_2 통합 |
