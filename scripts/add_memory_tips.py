#!/usr/bin/env python3
"""N5 단어 TSV에 암기 팁 컬럼을 추가하는 스크립트.
가타카나 전용 단어는 팁을 비워둠."""

import re
import time
import anthropic

INPUT_FILE = "scripts/final/n5words_with_examples.tsv"
OUTPUT_FILE = "scripts/final/n5words_with_tips.tsv"
BATCH_SIZE = 20


def is_katakana_word(text: str) -> bool:
    return bool(re.fullmatch(r"[ァ-ヺーヴ・]+", text.strip()))


def build_prompt(words: list[dict]) -> str:
    lines = []
    for w in words:
        lines.append(
            f"번호:{w['num']} / 단어:{w['word']} / 읽기:{w['reading']} / 뜻:{w['meaning']}"
        )
    word_list = "\n".join(lines)
    return f"""다음은 JLPT N5 일본어 단어 목록입니다.
각 단어에 대해 한국인이 쉽게 외울 수 있는 짧은 암기 팁을 만들어주세요.

규칙:
- 팁은 한국어로 작성
- 20자 이내로 간결하게
- 발음 연상, 한국어/영어 언어유희, 이미지 연상 중 가장 효과적인 방법 선택
- 억지스럽지 않게, 실제로 기억에 도움이 되는 것만
- 출력 형식: 번호:팁내용 (한 줄에 하나씩, 다른 설명 없이)

단어 목록:
{word_list}"""


def parse_response(text: str, nums: list[int]) -> dict[int, str]:
    tips: dict[int, str] = {}
    for line in text.strip().splitlines():
        line = line.strip()
        if not line:
            continue
        if ":" in line:
            parts = line.split(":", 1)
            try:
                num = int(parts[0].strip())
                tip = parts[1].strip()
                tips[num] = tip
            except ValueError:
                continue
    return tips


def main():
    client = anthropic.Anthropic()

    with open(INPUT_FILE, encoding="utf-8") as f:
        rows = f.readlines()

    # 가타카나 여부 판별 및 배치 구성
    non_katakana = []
    for row in rows:
        cols = row.strip().split("\t")
        if len(cols) < 4:
            continue
        num = int(cols[0])
        word = cols[1]
        reading = cols[2]
        meaning = cols[3]
        if not is_katakana_word(word):
            non_katakana.append(
                {"num": num, "word": word, "reading": reading, "meaning": meaning}
            )

    print(f"총 {len(rows)}개 중 비가타카나 {len(non_katakana)}개 처리 시작")

    all_tips: dict[int, str] = {}
    batches = [
        non_katakana[i : i + BATCH_SIZE]
        for i in range(0, len(non_katakana), BATCH_SIZE)
    ]

    for batch_idx, batch in enumerate(batches):
        nums = [w["num"] for w in batch]
        prompt = build_prompt(batch)
        print(f"  배치 {batch_idx + 1}/{len(batches)} (단어 번호 {nums[0]}~{nums[-1]}) ...", end=" ", flush=True)

        try:
            msg = client.messages.create(
                model="claude-haiku-4-5-20251001",
                max_tokens=1024,
                messages=[{"role": "user", "content": prompt}],
            )
            raw = msg.content[0].text
            tips = parse_response(raw, nums)
            all_tips.update(tips)
            print(f"완료 ({len(tips)}/{len(batch)}개 파싱)")
        except Exception as e:
            print(f"오류: {e}")

        time.sleep(0.3)

    # 출력 파일 생성
    with open(INPUT_FILE, encoding="utf-8") as f:
        rows = f.readlines()

    with open(OUTPUT_FILE, "w", encoding="utf-8") as out:
        for row in rows:
            cols = row.rstrip("\n").split("\t")
            if len(cols) < 1:
                out.write(row)
                continue
            try:
                num = int(cols[0])
            except ValueError:
                out.write(row + "\n")
                continue
            word = cols[1] if len(cols) > 1 else ""
            tip = all_tips.get(num, "") if not is_katakana_word(word) else ""
            out.write("\t".join(cols) + "\t" + tip + "\n")

    print(f"\n저장 완료: {OUTPUT_FILE}")
    print(f"팁 생성: {len(all_tips)}개 / 대상: {len(non_katakana)}개")


if __name__ == "__main__":
    main()
