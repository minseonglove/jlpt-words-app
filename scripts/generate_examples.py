#!/usr/bin/env python3
"""
JLPT 단어 예문 생성 스크립트
- 생각 에이전트: 각 단어에 맞는 예문을 구상
- 작성 에이전트: 구상한 예문을 TSV 형식으로 정리
- 체크포인트 기능: 중단 시 이어서 진행 가능
"""

import anthropic
import csv
import json
import os
import sys
import time
from pathlib import Path

# 설정
BATCH_SIZE = 20
INPUT_DIR = Path("/Users/gimminseong/jlpt-words/data-kmp/src/androidMain/res/raw")
OUTPUT_DIR = Path("/Users/gimminseong/jlpt-words/scripts/output")
CHECKPOINT_DIR = Path("/Users/gimminseong/jlpt-words/scripts/checkpoint")

OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
CHECKPOINT_DIR.mkdir(parents=True, exist_ok=True)

client = anthropic.Anthropic()

# ─────────────────────────────────────────────
# 프롬프트 정의
# ─────────────────────────────────────────────

THINKING_SYSTEM = """당신은 JLPT N1/N2 전문 일본어 교사입니다.
주어진 단어 목록을 보고, 각 단어에 대한 예문을 구상하는 역할입니다.

구상 원칙:
1. 뜻이 1개인 단어 → 예문 1개
2. 뜻이 2개 이상인 단어 → 처음 2개 뜻에 대해 각 1개씩 (최대 2개)
3. 각 예문은 해당 뜻을 명확하게 보여줘야 함
4. N1/N2 수준의 자연스러운 일본어 문장
5. 예문의 한국어 번역은 완성된 종결어미로 끝내야 함 (예: ~했다, ~이다, ~한다)
   - 금지: 사전형(~하다), 명사화(~함, ~것)로 끝나는 형태
6. 예문에 쓰인 단어는 원형으로 추출 (동사는 辞書形, 형용사는 원형)

각 단어에 대해 JSON 형식으로 구상 결과를 제시하세요."""

THINKING_USER_TEMPLATE = """다음 {level} 단어들에 대해 예문을 구상하세요:

{words_json}

각 단어에 대해 다음 형식으로 JSON 배열을 반환하세요:
[
  {{
    "word": "단어",
    "reading": "발음",
    "meanings": ["뜻1", "뜻2"],
    "examples": [
      {{
        "ja": "일본어 예문",
        "ko": "한국어 번역 (완성된 종결어미)",
        "words": ["원형단어1", "원형단어2"]
      }}
    ]
  }}
]

반드시 유효한 JSON만 반환하고 다른 텍스트는 포함하지 마세요."""

WRITING_SYSTEM = """당신은 데이터 정리 전문가입니다.
주어진 예문 구상 결과를 정확한 TSV 형식으로 변환하는 역할입니다.

출력 형식 (탭 구분):
번호\t단어\t발음\t뜻\t품사\t예문\t예문의뜻\t예문에 쓰인 단어

규칙:
- 모든 필드는 탭(\t)으로 구분
- 예문이 2개인 경우: 예문1||예문2
- 예문의뜻이 2개인 경우: 뜻1\\n뜻2
- 예문에 쓰인 단어가 2개 세트인 경우: 단어1,단어2||단어3,단어4
- 번호는 전체 파일에서의 순서 번호"""

WRITING_USER_TEMPLATE = """다음 구상 결과를 TSV 형식으로 변환하세요.

시작 번호: {start_num}
원본 데이터 (뜻, 품사 정보 포함):
{original_data}

구상된 예문:
{thinking_result}

정확히 다음 TSV 형식으로 출력하세요 (헤더 없이 데이터만):
번호\t단어\t발음\t뜻\t품사\t예문\t예문의뜻\t예문에 쓰인 단어

주의:
- 헤더 행 없이 데이터 행만 출력
- 각 단어는 반드시 1개 행
- 예문 2개면: 예문1||예문2 (탭이 아닌 ||로 구분)
- 예문의뜻 2개면: 뜻1\\n뜻2 (탭이 아닌 \\n으로 구분)
- 예문에 쓰인 단어 2세트면: 단어1,단어2||단어3,단어4"""

# ─────────────────────────────────────────────
# CSV 파싱
# ─────────────────────────────────────────────

def parse_csv(filepath: Path) -> list[dict]:
    """CSV 파일을 파싱하여 단어 목록 반환"""
    words = []
    with open(filepath, "r", encoding="utf-8") as f:
        reader = csv.reader(f)
        for i, row in enumerate(reader, 1):
            if len(row) < 4:
                continue
            word, reading, meaning, pos = row[0], row[1], row[2], row[3]
            meanings = [m.strip() for m in meaning.replace("\\n", "\n").split("\n") if m.strip()]
            words.append({
                "num": i,
                "word": word,
                "reading": reading,
                "meaning_raw": meaning,
                "meanings": meanings,
                "pos": pos,
            })
    return words

# ─────────────────────────────────────────────
# API 호출
# ─────────────────────────────────────────────

def call_thinking_agent(batch: list[dict], level: str) -> str:
    """생각 에이전트: 예문 구상"""
    words_for_thinking = []
    for w in batch:
        # 뜻이 2개 이상이면 처음 2개만
        meanings_to_use = w["meanings"][:2] if len(w["meanings"]) >= 2 else w["meanings"]
        words_for_thinking.append({
            "word": w["word"],
            "reading": w["reading"],
            "meanings": meanings_to_use,
            "pos": w["pos"],
            "example_count": len(meanings_to_use)
        })

    words_json = json.dumps(words_for_thinking, ensure_ascii=False, indent=2)
    user_msg = THINKING_USER_TEMPLATE.format(level=level, words_json=words_json)

    for attempt in range(3):
        try:
            response = client.messages.create(
                model="claude-sonnet-4-6",
                max_tokens=8192,
                system=THINKING_SYSTEM,
                messages=[{"role": "user", "content": user_msg}]
            )
            return response.content[0].text
        except Exception as e:
            print(f"  생각 에이전트 오류 (시도 {attempt+1}/3): {e}")
            time.sleep(5 * (attempt + 1))

    raise RuntimeError("생각 에이전트 실패")

def call_writing_agent(batch: list[dict], thinking_result: str, start_num: int) -> str:
    """작성 에이전트: TSV 형식으로 변환"""
    original_lines = []
    for w in batch:
        original_lines.append(
            f"번호={w['num']}, 단어={w['word']}, 발음={w['reading']}, "
            f"뜻={w['meaning_raw']}, 품사={w['pos']}"
        )
    original_data = "\n".join(original_lines)

    user_msg = WRITING_USER_TEMPLATE.format(
        start_num=start_num,
        original_data=original_data,
        thinking_result=thinking_result
    )

    for attempt in range(3):
        try:
            response = client.messages.create(
                model="claude-sonnet-4-6",
                max_tokens=8192,
                system=WRITING_SYSTEM,
                messages=[{"role": "user", "content": user_msg}]
            )
            return response.content[0].text
        except Exception as e:
            print(f"  작성 에이전트 오류 (시도 {attempt+1}/3): {e}")
            time.sleep(5 * (attempt + 1))

    raise RuntimeError("작성 에이전트 실패")

# ─────────────────────────────────────────────
# 체크포인트
# ─────────────────────────────────────────────

def load_checkpoint(level: str) -> int:
    """마지막 처리된 배치 번호 로드"""
    ckpt_file = CHECKPOINT_DIR / f"{level}_checkpoint.json"
    if ckpt_file.exists():
        with open(ckpt_file) as f:
            data = json.load(f)
            return data.get("last_batch", 0)
    return 0

def save_checkpoint(level: str, batch_num: int):
    """체크포인트 저장"""
    ckpt_file = CHECKPOINT_DIR / f"{level}_checkpoint.json"
    with open(ckpt_file, "w") as f:
        json.dump({"last_batch": batch_num}, f)

# ─────────────────────────────────────────────
# 메인 처리
# ─────────────────────────────────────────────

def process_level(level: str):
    """레벨별 처리 (n1 또는 n2)"""
    input_file = INPUT_DIR / f"{level}words_shuffled.csv"
    output_file = OUTPUT_DIR / f"{level}words_with_examples.tsv"

    print(f"\n{'='*60}")
    print(f"{level.upper()} 처리 시작")
    print(f"입력: {input_file}")
    print(f"출력: {output_file}")
    print(f"{'='*60}")

    words = parse_csv(input_file)
    total = len(words)
    total_batches = (total + BATCH_SIZE - 1) // BATCH_SIZE

    print(f"총 {total}개 단어, {total_batches}개 배치 (배치당 {BATCH_SIZE}개)")

    start_batch = load_checkpoint(level)
    if start_batch > 0:
        print(f"체크포인트에서 재시작: 배치 {start_batch + 1}부터")

    # 출력 파일 (이어쓰기 또는 새로 시작)
    file_mode = "a" if start_batch > 0 else "w"

    with open(output_file, file_mode, encoding="utf-8") as out_f:
        for batch_idx in range(start_batch, total_batches):
            batch_start = batch_idx * BATCH_SIZE
            batch_end = min(batch_start + BATCH_SIZE, total)
            batch = words[batch_start:batch_end]

            print(f"\n배치 {batch_idx + 1}/{total_batches} "
                  f"(단어 {batch_start + 1}~{batch_end})")

            # 1단계: 생각 에이전트
            print("  [생각 에이전트] 예문 구상 중...")
            thinking_result = call_thinking_agent(batch, level.upper())

            # 2단계: 작성 에이전트
            print("  [작성 에이전트] TSV 형식으로 변환 중...")
            tsv_output = call_writing_agent(batch, thinking_result, batch_start + 1)

            # 결과 저장
            # 불필요한 마크다운 코드블록 제거
            tsv_clean = tsv_output.strip()
            if tsv_clean.startswith("```"):
                lines = tsv_clean.split("\n")
                lines = [l for l in lines if not l.startswith("```")]
                tsv_clean = "\n".join(lines)

            out_f.write(tsv_clean + "\n")
            out_f.flush()

            # 체크포인트 저장
            save_checkpoint(level, batch_idx + 1)

            print(f"  ✓ 완료 ({batch_end}/{total})")

            # API 레이트 리밋 방지
            if batch_idx < total_batches - 1:
                time.sleep(1)

    print(f"\n{level.upper()} 처리 완료!")
    print(f"출력 파일: {output_file}")

    # 체크포인트 삭제 (완료)
    ckpt_file = CHECKPOINT_DIR / f"{level}_checkpoint.json"
    if ckpt_file.exists():
        ckpt_file.unlink()

# ─────────────────────────────────────────────
# 진입점
# ─────────────────────────────────────────────

def main():
    levels = sys.argv[1:] if len(sys.argv) > 1 else ["n1", "n2"]

    for level in levels:
        if level not in ["n1", "n2"]:
            print(f"알 수 없는 레벨: {level} (n1 또는 n2만 지원)")
            continue
        process_level(level)

    print("\n모든 처리 완료!")

if __name__ == "__main__":
    main()
