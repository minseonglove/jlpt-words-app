#!/usr/bin/env python3
"""
all_word_meanings_marked.tsv 전수 검사.
* 개수 기준: valid=2, missing=0, malformed=기타
"""
import random
from collections import defaultdict
from pathlib import Path

BASE     = Path(__file__).parent
TSV_PATH = BASE / "meaning_gen" / "all_word_meanings_marked.tsv"
UNMATCH  = BASE / "meaning_gen" / "highlight_unmatched.tsv"


def main() -> None:
    total = valid = missing = malformed = 0
    by_level: dict[str, list[tuple]] = defaultdict(list)

    with open(TSV_PATH, encoding="utf-8") as f:
        for line in f:
            cols = line.rstrip("\n").split("\t")
            if len(cols) < 9:
                continue
            level, source_word, example_jp = cols[0], cols[1], cols[3]
            total += 1

            cnt = example_jp.count("*")
            if cnt == 2:
                valid += 1
                status = "OK"
            elif cnt == 0:
                missing += 1
                status = "MISSING"
            else:
                malformed += 1
                status = f"MALFORMED({cnt}*)"
                print(f"  ⚠ {status}: [{level}] {source_word} → {example_jp[:60]}")

            by_level[level].append((source_word, example_jp, status))

    # ── 레벨별 샘플 ────────────────────────────────────────────
    print("\n=== 레벨별 샘플 (valid 5개) ===")
    for lv in ["n1", "n2", "n3", "n4", "n5"]:
        valid_rows = [(w, ex) for w, ex, s in by_level[lv] if s == "OK"]
        sample = random.sample(valid_rows, min(5, len(valid_rows)))
        print(f"\n[{lv}]")
        for w, ex in sample:
            print(f"  {w}: {ex[:70]}")

    # ── 요약 ───────────────────────────────────────────────────
    unmatch_count = 0
    if UNMATCH.exists():
        with open(UNMATCH) as f:
            unmatch_count = sum(1 for _ in f) - 1  # 헤더 제외

    print("\n=== 전수 검사 결과 ===")
    print(f"전체:     {total:,}행")
    print(f"valid:    {valid:,}  ({valid/total*100:.1f}%)")
    print(f"missing:  {missing:,}  ({missing/total*100:.1f}%)")
    print(f"malformed:{malformed:,}  ({malformed/total*100:.1f}%)")
    print(f"미매칭 로그: {unmatch_count}건 ({UNMATCH.name})")


if __name__ == "__main__":
    main()
