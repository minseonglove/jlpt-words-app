#!/usr/bin/env python3
"""
배치 TSV 파일들을 하나의 최종 파일로 합산
- 번호를 1부터 순서대로 재부여
- 누락된 배치 확인
- 열 개수 검증
"""

import os
import re
from pathlib import Path

OUTPUT_DIR = Path("/Users/gimminseong/jlpt-words/scripts/output")
FINAL_DIR = Path("/Users/gimminseong/jlpt-words/scripts/final")
FINAL_DIR.mkdir(exist_ok=True)

EXPECTED_COLS = 8
HEADER = "번호\t단어\t발음\t뜻\t품사\t예문\t예문의뜻\t예문에 쓰인 단어"


def combine(level: str, total_batches: int):
    print(f"\n{'='*50}")
    print(f"{level.upper()} 합산 시작 (총 {total_batches}개 배치)")
    print(f"{'='*50}")

    final_file = FINAL_DIR / f"{level}words_with_examples.tsv"
    missing = []
    total_rows = 0
    error_rows = []
    row_num = 1

    with open(final_file, "w", encoding="utf-8") as out:
        out.write(HEADER + "\n")

        for i in range(1, total_batches + 1):
            batch_file = OUTPUT_DIR / f"{level}_batch_{i:03d}.tsv"

            if not batch_file.exists():
                missing.append(i)
                print(f"  ⚠️  배치 {i:03d} 누락")
                continue

            with open(batch_file, "r", encoding="utf-8") as f:
                lines = [l.rstrip('\n') for l in f if l.strip()]

            batch_rows = 0
            for line in lines:
                # 마크다운 코드블록 제거
                if line.startswith('```') or line == '```':
                    continue
                cols = line.split('\t')
                if len(cols) < EXPECTED_COLS:
                    error_rows.append((i, line[:80]))
                    # 열이 부족해도 일단 포함
                # 번호 재부여
                cols[0] = str(row_num)
                out.write('\t'.join(cols) + '\n')
                row_num += 1
                batch_rows += 1

            total_rows += batch_rows
            print(f"  ✓ 배치 {i:03d}: {batch_rows}행")

    print(f"\n결과:")
    print(f"  총 행: {total_rows}개")
    print(f"  누락 배치: {missing if missing else '없음'}")
    print(f"  형식 오류 행: {len(error_rows)}개")
    if error_rows:
        for batch_num, line in error_rows[:5]:
            print(f"    배치 {batch_num}: {line}")
    print(f"  출력: {final_file}")

    return missing


def check_progress(level: str, total_batches: int):
    """완료된 배치 파일 수 확인"""
    done = sum(
        1 for i in range(1, total_batches + 1)
        if (OUTPUT_DIR / f"{level}_batch_{i:03d}.tsv").exists()
    )
    print(f"{level.upper()}: {done}/{total_batches} 배치 완료")
    return done


if __name__ == "__main__":
    import sys

    if "--check" in sys.argv:
        # 진행 상황만 확인
        check_progress("n1", 31)
        check_progress("n2", 25)
    else:
        # 합산 실행
        n1_missing = combine("n1", 31)
        n2_missing = combine("n2", 25)

        if n1_missing or n2_missing:
            print(f"\n⚠️  누락된 배치가 있습니다. 해당 에이전트 완료 후 재실행하세요.")
        else:
            print(f"\n✅ 모든 배치 합산 완료!")
