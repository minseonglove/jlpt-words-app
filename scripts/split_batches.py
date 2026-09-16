#!/usr/bin/env python3
"""CSV 파일을 배치 JSON 파일로 분할"""

import csv
import json
from pathlib import Path

INPUT_DIR = Path("/Users/gimminseong/jlpt-words/data-kmp/src/androidMain/res/raw")
BATCH_DIR = Path("/Users/gimminseong/jlpt-words/scripts/batches")
BATCH_SIZE = 100

def parse_meanings(meaning_raw):
    import re
    parts = re.split(r'\\\\n', meaning_raw)
    cleaned = []
    for p in parts:
        p = p.rstrip('\\')
        p = re.sub(r'^\d+\.', '', p).strip()
        if p:
            cleaned.append(p)
    return cleaned

def parse_csv(filepath):
    words = []
    with open(filepath, "r", encoding="utf-8") as f:
        reader = csv.reader(f)
        for i, row in enumerate(reader, 1):
            if len(row) < 4:
                continue
            word, reading, meaning, pos = row[0], row[1], row[2], row[3]
            meanings = parse_meanings(meaning)
            words.append({
                "num": i,
                "word": word,
                "reading": reading,
                "meaning_raw": meaning,
                "meanings": meanings,
                "pos": pos,
            })
    return words

for level in ["n1", "n2"]:
    words = parse_csv(INPUT_DIR / f"{level}words_shuffled.csv")
    out_dir = BATCH_DIR / level
    batches = [words[i:i+BATCH_SIZE] for i in range(0, len(words), BATCH_SIZE)]

    for idx, batch in enumerate(batches):
        out_file = out_dir / f"batch_{idx+1:03d}.json"
        with open(out_file, "w", encoding="utf-8") as f:
            json.dump(batch, f, ensure_ascii=False, indent=2)

    print(f"{level.upper()}: {len(words)}개 단어 → {len(batches)}개 배치")

print("분할 완료!")
