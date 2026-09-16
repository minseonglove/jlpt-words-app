# Firestore 업로드 (신규 컬렉션)

기존 컬렉션(n1~n5, words_update_date)은 건드리지 않고 신규 컬렉션만 적재한다.

## 전제
- Firebase 서비스 계정 키 JSON (프로젝트 설정 > 서비스 계정 > 새 비공개 키)
- 환경변수: `export GOOGLE_APPLICATION_CREDENTIALS=/abs/path/serviceAccount.json`

## 설치
    python3 -m venv .venv && source .venv/bin/activate
    pip install -r requirements.txt

## 실행
    # dry-run: 적재 없이 파싱 결과 통계만 출력
    python upload.py --dry-run
    # 실제 적재
    python upload.py
    # 적재 검증
    python verify.py

## 테스트
    pytest test_parsing.py -v
