#!/usr/bin/env python3
"""
highlight_batches/results/ 디렉토리의 배치 결과를 원본 TSV에 적용해 marked 버전 생성.
"""
import json
from pathlib import Path

BASE         = Path(__file__).parent
TSV_IN       = BASE / "meaning_gen" / "all_word_meanings.tsv"
TSV_OUT      = BASE / "meaning_gen" / "all_word_meanings_marked.tsv"
UNMATCH_OUT  = BASE / "meaning_gen" / "highlight_unmatched.tsv"
RESULTS_DIR  = BASE / "highlight_batches" / "results"


def main() -> None:
    # ── 1. 배치 결과 파일 전부 로드 ─────────────────────────────
    mapping: dict[str, str] = {}
    result_files = sorted(RESULTS_DIR.glob("result_*.json"))

    if not result_files:
        print(f"ERROR: {RESULTS_DIR} 에 결과 파일이 없습니다.")
        return

    for rf in result_files:
        with open(rf, encoding="utf-8") as f:
            data = json.load(f)
        for item in data:
            mapping[item["key"]] = item["marked"]

    print(f"결과 파일: {len(result_files)}개, 매핑 항목: {len(mapping)}개")

    # ── 2. TSV 처리 ─────────────────────────────────────────────
    total = modified = 0
    unmatched_rows: list[list[str]] = []

    with open(TSV_IN, encoding="utf-8") as fin, \
         open(TSV_OUT, "w", encoding="utf-8") as fout:
        for line in fin:
            cols = line.rstrip("\n").split("\t")
            total += 1

            if len(cols) >= 9:
                key = f"{cols[1]}|||{cols[3]}"
                if key in mapping:
                    marked = mapping[key]
                    if marked != cols[3]:
                        cols[3] = marked
                        modified += 1
                if "*" not in cols[3]:
                    unmatched_rows.append(cols[:])

            fout.write("\t".join(cols) + "\n")

    # ── 3. 미매칭 로그 ──────────────────────────────────────────
    with open(UNMATCH_OUT, "w", encoding="utf-8") as f:
        f.write("level\tsource_word\tsource_pron\texample_jp\texample_ko\n")
        for cols in unmatched_rows:
            f.write("\t".join(cols[:5]) + "\n")

    print(f"전체 행: {total:,}, 수정됨: {modified:,}, 미매칭: {len(unmatched_rows):,}")
    print(f"출력: {TSV_OUT.name}")
    print(f"미매칭 로그: {UNMATCH_OUT.name}")


if __name__ == "__main__":
    main()
