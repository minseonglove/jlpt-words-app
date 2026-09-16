#!/usr/bin/env python3
"""
all_word_meanings.tsv에서 unique (source_word, example_jp) 쌍을 추출하여
highlight_batches/ 디렉토리에 배치 JSON 파일로 저장.
API 불필요 — 순수 데이터 전처리.
"""
import json
from pathlib import Path

BASE       = Path(__file__).parent
TSV_PATH   = BASE / "meaning_gen" / "all_word_meanings.tsv"
BATCHES_DIR = BASE / "highlight_batches"
BATCH_SIZE  = 25

BATCHES_DIR.mkdir(exist_ok=True)


def main() -> None:
    pairs: dict[str, dict] = {}

    with open(TSV_PATH, encoding="utf-8") as f:
        for line in f:
            cols = line.rstrip("\n").split("\t")
            if len(cols) < 9:
                continue
            sw, ex = cols[1], cols[3]
            key = f"{sw}|||{ex}"
            if key not in pairs:
                idx = sw.find("(")
                base_form = sw[:idx].strip() if idx != -1 else sw.strip()
                pairs[key] = {"key": key, "word": base_form, "sentence": ex}

    pairs_list = list(pairs.values())
    total = len(pairs_list)
    total_batches = (total + BATCH_SIZE - 1) // BATCH_SIZE

    batch_files = []
    for i in range(0, total, BATCH_SIZE):
        batch = pairs_list[i : i + BATCH_SIZE]
        num = i // BATCH_SIZE
        path = BATCHES_DIR / f"batch_{num:04d}.json"
        with open(path, "w", encoding="utf-8") as f:
            json.dump(batch, f, ensure_ascii=False)
        batch_files.append(str(path))

    index = {
        "total_pairs": total,
        "total_batches": total_batches,
        "batch_size": BATCH_SIZE,
        "files": batch_files,
    }
    with open(BATCHES_DIR / "index.json", "w", encoding="utf-8") as f:
        json.dump(index, f, ensure_ascii=False, indent=2)

    print(f"unique 쌍: {total}개 → {total_batches}개 배치 파일 (배치당 {BATCH_SIZE}개)")
    print(f"저장 위치: {BATCHES_DIR}")


if __name__ == "__main__":
    main()
