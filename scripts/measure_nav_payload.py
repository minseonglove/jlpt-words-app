# -*- coding: utf-8 -*-
"""nav 직렬화 페이로드 검증: resume 시 StudyNavType이 Bundle에 넣는 JSON 크기를 실측.

앱의 kotlinx.serialization Json.Default 출력(공백 없는 compact, 비ASCII는 UTF-8 그대로)을
모사하여 WordDTO JSON 바이트 크기를 단어별로 계산하고, 세션 타입별 worst-case 합계를 낸다.
"""
import json
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "firestore_upload"))
import parsing  # noqa: E402

FINAL_DIR = os.path.join(os.path.dirname(__file__), "final")
MEANINGS = os.path.join(os.path.dirname(__file__), "meaning_gen", "all_word_meanings.tsv")
LEVELS = ["n1", "n2", "n3", "n4", "n5"]

# 세션 타입별 단어 수 상한 (CLAUDE.md StudySessionGenerator)
SESSION_CAPS = {"RANDOM": 300, "LOW_ACCURACY": 100, "NORMAL(window)": 50}


def word_dto_json_bytes(word_body, examples_for_kanji, readings):
    """앱 WordDTO와 동일 구조의 dict를 compact JSON(UTF-8)으로 직렬화한 바이트 수."""
    examples = []
    for ex in examples_for_kanji:
        tokens = [
            {"surface": t["surface"], "reading": readings.get(t["surface"], ""), "meaning": t["meaning"]}
            for t in ex["tokens"]
        ]
        examples.append({"japanese": ex["jp"], "korean": ex["ko"], "tokens": tokens})
    max_ex = os.environ.get("MAX_EX")  # MAX_EX=1 → 예문 1개만 전송 가정
    if max_ex:
        examples = examples[: int(max_ex)]
    dto = {
        "id": 0,
        "kanji": word_body["kanji"],
        "pronunciation": word_body["pronunciation"],
        "meaning": word_body["meaning"],
        "partOfSpeech": word_body["partOfSpeech"],
        "appearanceCount": 0,
        "correctCount": 0,
    }
    if not os.environ.get("NO_EX"):  # NO_EX=1 → 예문 필드 자체 제거(0개 안). 채택안은 MAX_EX=1(1개)
        dto["examples"] = examples
    # ensure_ascii=False → 일본어/한국어를 UTF-8 그대로 (kotlinx와 동일), separators로 compact화
    return len(json.dumps(dto, ensure_ascii=False, separators=(",", ":")).encode("utf-8"))


def main():
    with open(MEANINGS, encoding="utf-8") as f:
        meaning_lines = f.readlines()
    examples_all = parsing.parse_examples(meaning_lines)
    readings = parsing.parse_readings(meaning_lines)

    print(f"{'level':6} {'words':>6} {'avg':>7} {'p50':>7} {'p99':>7} {'max':>7}  (단어당 WordDTO JSON bytes)")
    sizes_by_level = {}
    for level in LEVELS:
        words = parsing.parse_words(
            open(f"{FINAL_DIR}/{level}words_with_examples.tsv", encoding="utf-8").readlines()
        )
        ex_for_level = examples_all.get(level, {})
        sizes = [word_dto_json_bytes(w, ex_for_level.get(w["kanji"], []), readings) for w in words]
        sizes.sort()
        sizes_by_level[level] = sizes
        n = len(sizes)
        avg = sum(sizes) / n
        p50 = sizes[n // 2]
        p99 = sizes[int(n * 0.99)]
        print(f"{level:6} {n:6d} {avg:7.0f} {p50:7d} {p99:7d} {sizes[-1]:7d}")

    print("\n=== 세션 worst-case nav 페이로드 (해당 레벨 상위 N개 단어 합) ===")
    print("Android Binder 트랜잭션 한계 ≈ 1MB(1,048,576). 실무 위험선 ≈ 500KB.\n")
    for level in LEVELS:
        sizes = sizes_by_level[level]
        biggest = sizes[::-1]
        row = [level]
        for cap in SESSION_CAPS.values():
            take = biggest[: min(cap, len(sizes))]
            row.append(sum(take))
        print(
            f"[{level}] RANDOM(≤300)={row[1]:>9,}B  "
            f"LOW_ACC(≤100)={row[2]:>8,}B  NORMAL_win(≤50)={row[3]:>8,}B"
        )

    print("\n=== 평균 기반 추정 (avg × cap) ===")
    for level in LEVELS:
        sizes = sizes_by_level[level]
        avg = sum(sizes) / len(sizes)
        print(
            f"[{level}] RANDOM={avg*300:>9,.0f}B  "
            f"LOW_ACC={avg*100:>8,.0f}B  NORMAL_win={avg*50:>8,.0f}B"
        )


if __name__ == "__main__":
    main()
