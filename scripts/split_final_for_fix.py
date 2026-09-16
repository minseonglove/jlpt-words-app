#!/usr/bin/env python3
"""최종 TSV를 100행 배치로 분할 (검수용)"""

from pathlib import Path

FINAL_DIR = Path("/Users/gimminseong/jlpt-words/scripts/final")
FIX_DIR = Path("/Users/gimminseong/jlpt-words/scripts/fix_batches")

for level in ["n1", "n2"]:
    out_dir = FIX_DIR / level
    out_dir.mkdir(parents=True, exist_ok=True)

    final_file = FINAL_DIR / f"{level}words_with_examples.tsv"
    lines = final_file.read_text(encoding="utf-8").splitlines()

    # 헤더 제거
    header = lines[0]
    data = [l for l in lines[1:] if l.strip()]

    batch_size = 100
    batches = [data[i:i+batch_size] for i in range(0, len(data), batch_size)]

    for idx, batch in enumerate(batches):
        out_file = out_dir / f"batch_{idx+1:03d}.tsv"
        out_file.write_text("\n".join(batch) + "\n", encoding="utf-8")

    print(f"{level.upper()}: {len(data)}행 → {len(batches)}개 배치")

print("분할 완료!")
