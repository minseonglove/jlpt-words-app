#!/usr/bin/env python3
"""N3/N4/N5 CSV → 배치 TSV 파일 생성 (5컬럼: 번호\t단어\t발음\t뜻\t품사)"""
import csv
import os
from pathlib import Path

BASE = Path("/Users/gimminseong/jlpt-words")
SRC = BASE / "data-kmp/src/iosMain/resources"
DST = BASE / "scripts/fix_batches"

LEVELS = {
    "n3": SRC / "n3words_shuffled.csv",
    "n4": SRC / "n4words_shuffled.csv",
    "n5": SRC / "n5words_shuffled.csv",
}

BATCH_SIZE = 100

for level, src_file in LEVELS.items():
    rows = []
    with open(src_file, encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if not line.strip():
                continue
            parts = line.split(",", 3)  # 단어,발음,뜻,품사
            if len(parts) < 3:
                continue
            rows.append(parts)

    total = len(rows)
    num_batches = (total + BATCH_SIZE - 1) // BATCH_SIZE
    print(f"{level}: {total} 단어 → {num_batches} 배치")

    out_dir = DST / level
    for i in range(num_batches):
        batch_rows = rows[i * BATCH_SIZE : (i + 1) * BATCH_SIZE]
        batch_num = i + 1
        filename = out_dir / f"batch_{batch_num:03d}.tsv"
        with open(filename, "w", encoding="utf-8") as f:
            for j, parts in enumerate(batch_rows):
                seq = i * BATCH_SIZE + j + 1
                word   = parts[0] if len(parts) > 0 else ""
                reading = parts[1] if len(parts) > 1 else ""
                meaning = parts[2] if len(parts) > 2 else ""
                pos    = parts[3] if len(parts) > 3 else ""
                f.write(f"{seq}\t{word}\t{reading}\t{meaning}\t{pos}\n")
        print(f"  {filename.name}: {len(batch_rows)} 행")

print("완료")
