#!/usr/bin/env python3
"""N3/N4/N5 fix_output 배치 파일을 합산하여 최종 TSV 파일 생성"""
from pathlib import Path
import sys

BASE = Path("/Users/gimminseong/jlpt-words/scripts")
OUT_DIR = BASE / "final"
OUT_DIR.mkdir(parents=True, exist_ok=True)

EXPECTED_COLS = 8

def fix_row(cols, line_num):
    n = len(cols)
    if n == EXPECTED_COLS:
        return cols
    if n < EXPECTED_COLS:
        # 빈 품사 등으로 인한 짧은 행은 그대로 패딩
        if n >= 5:
            while len(cols) < EXPECTED_COLS:
                cols.append("")
            return cols
        return None
    if n == 9:
        # 에이전트가 순번을 앞에 추가한 경우: col[0]=순번, col[1]=실제번호
        try:
            seq = int(cols[0])
            actual_num = int(cols[1])
            if seq >= 1 and seq <= 200 and actual_num >= 1:
                return cols[1:]
        except (ValueError, IndexError):
            pass
        # 그냥 앞 컬럼 잘라내기
        return cols[1:]
    # 10컬럼 이상: 앞에서 자르기
    return cols[:EXPECTED_COLS]

for level, num_batches in [("n3", 16), ("n4", 10), ("n5", 7)]:
    in_dir = BASE / "fix_output" / level
    out_file = OUT_DIR / f"{level}words_with_examples.tsv"

    all_rows = []
    errors = []

    for i in range(1, num_batches + 1):
        batch_file = in_dir / f"batch_{i:03d}.tsv"
        if not batch_file.exists():
            print(f"  [경고] {batch_file.name} 없음", file=sys.stderr)
            continue

        with open(batch_file, encoding="utf-8") as f:
            for line_num, line in enumerate(f, 1):
                line = line.rstrip("\n")
                if not line.strip():
                    continue
                cols = line.split("\t")
                fixed = fix_row(cols, line_num)
                if fixed is None:
                    errors.append(f"{batch_file.name}:{line_num} — {len(cols)}컬럼")
                    continue
                # 예문 컬럼(index 5)이 비어있으면 스킵
                if len(fixed) < 6 or not fixed[5].strip():
                    errors.append(f"{batch_file.name}:{line_num} — 예문 없음")
                all_rows.append(fixed)

    with open(out_file, "w", encoding="utf-8") as f:
        for row in all_rows:
            f.write("\t".join(row) + "\n")

    # 검증
    bad = [i+1 for i, r in enumerate(all_rows) if len(r) != EXPECTED_COLS]
    print(f"{level}: {len(all_rows)}행 → {out_file.name}")
    if bad:
        print(f"  [오류] {len(bad)}행 컬럼 불일치: {bad[:10]}")
    else:
        print(f"  형식 오류: 0")
    if errors:
        print(f"  [경고] {len(errors)}건:\n" + "\n".join(f"    {e}" for e in errors[:10]))

print("완료")
