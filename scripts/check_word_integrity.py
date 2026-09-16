# -*- coding: utf-8 -*-
"""단어 본체 TSV 정합성 스캔: 컬럼 시프트/오염 탐지.

컬럼: id, 단어(kanji), 발음(pron, 가나), 뜻(meaning, 한글), 품사(pos), 예문...
규칙 위반(시프트 의심):
 - meaning(뜻)에 한글이 전혀 없음 → 빈칸이거나 일본어가 밀려 들어옴
 - pron(발음)에 한글 포함 → 뜻이 발음칸으로 밀림
 - pos(품사)에 일본어(가나/한자) 포함 → 예문/단어가 품사칸으로 밀림(오염)
 - NF != 8 → 구조 깨짐
빈 POS가 위 이상과 동반되는지를 핵심으로 본다.
"""
import os
import re

LEVELS = ["n1", "n2", "n3", "n4", "n5"]
FINAL = os.path.join(os.path.dirname(__file__), "final")

KANA = re.compile(r"[぀-ゟ゠-ヿ]")          # 히라가나/가타카나
CJK = re.compile(r"[一-鿿]")                         # 한자
HANGUL = re.compile(r"[가-힣]")


def has_jp(s):
    return bool(KANA.search(s) or CJK.search(s))


def has_kr(s):
    return bool(HANGUL.search(s))


def main():
    grand = {"meaning_no_kr": [], "pron_has_kr": [], "pos_has_jp": [], "nf_bad": []}
    empty_pos_with_issue = []

    for level in LEVELS:
        path = f"{FINAL}/{level}words_with_examples.tsv"
        rows = total = empty_pos = 0
        for line in open(path, encoding="utf-8"):
            c = line.rstrip("\n").split("\t")
            if len(c) < 5 or not c[0].strip().isdigit():
                continue
            total += 1
            nf = len(c)
            kanji, pron, meaning, pos = c[1], c[2], c[3], c[4]
            is_empty_pos = pos.strip() in ("", "　")

            meaning_no_kr = not has_kr(meaning)
            pron_has_kr = has_kr(pron)
            pos_has_jp = has_jp(pos)
            nf_bad = nf != 8

            tag = f"{level} [{c[0]}] 단어={kanji} 발음={pron} 뜻={meaning} 품사=[{pos}] NF={nf}"
            if meaning_no_kr:
                grand["meaning_no_kr"].append(tag)
            if pron_has_kr:
                grand["pron_has_kr"].append(tag)
            if pos_has_jp:
                grand["pos_has_jp"].append(tag)
            if nf_bad:
                grand["nf_bad"].append(tag)
            if is_empty_pos:
                empty_pos += 1
                if meaning_no_kr or pron_has_kr or nf_bad:
                    empty_pos_with_issue.append(tag)
        print(f"[{level}] rows={total} emptyPOS={empty_pos}")

    print("\n=== 핵심: 빈 POS 행이 다른 필드 이상을 동반하는가 ===")
    if empty_pos_with_issue:
        for t in empty_pos_with_issue:
            print("  ⚠ " + t)
    else:
        print("  ✅ 없음 — 빈 POS 행들의 단어/발음/뜻은 모두 정상(품사만 비어있음)")

    print(f"\n=== 전체 의심 행 (POS 무관) ===")
    print(f"[뜻에 한글 없음] {len(grand['meaning_no_kr'])}건")
    for t in grand["meaning_no_kr"][:40]:
        print("  " + t)
    print(f"[발음에 한글 포함(뜻이 밀림 의심)] {len(grand['pron_has_kr'])}건")
    for t in grand["pron_has_kr"][:40]:
        print("  " + t)
    print(f"[품사에 일본어(오염)] {len(grand['pos_has_jp'])}건")
    for t in grand["pos_has_jp"][:40]:
        print("  " + t)
    print(f"[NF!=8 구조깨짐] {len(grand['nf_bad'])}건")
    for t in grand["nf_bad"][:20]:
        print("  " + t)


if __name__ == "__main__":
    main()
