#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
최종 설계 구조의 실제 Firestore 문서 크기 측정.
- 예문 컬렉션:   kanji -> [{jp, ko, tokens:[{surface, meaning}]}]   (문맥뜻 포함, 요미 제외)
- 요미 컬렉션:   surface -> yomi                                     (전역 사전, 불변)
둘 다 "레벨 전체 = 문서 1개"가 1MB 안에 들어가는지 검증.
"""
import json
from collections import defaultdict

PATH = "meaning_gen/all_word_meanings.tsv"
ONE_MB = 1048576

# level -> kanji -> meaning_index -> {jp, ko, morphemes:[{surface,yomi,meaning}]}
data = defaultdict(lambda: defaultdict(dict))
with open(PATH, encoding="utf-8") as f:
    for line in f:
        parts = line.rstrip("\n").split("\t")
        if len(parts) < 9:
            continue
        level, sw, sp, ejp, eko, midx, vw, vp, mic = parts[:9]
        d = data[level][sw]
        if midx not in d:
            d[midx] = {"jp": ejp, "ko": eko, "morphemes": []}
        d[midx]["morphemes"].append({"surface": vw, "yomi": vp, "meaning": mic})


def jb(obj):
    return len(json.dumps(obj, ensure_ascii=False).encode("utf-8"))


print(f"{'레벨':<5}{'단어':>6}  {'예문컬렉션(문맥뜻포함)':>26}{'레벨1문서':>10}   "
      f"{'요미사전(전역)':>20}{'레벨1문서':>10}   {'단어당예문 평균/최대':>20}")
for level in ["n1", "n2", "n3", "n4", "n5"]:
    words = data[level]
    n = len(words)

    ex_doc = {}            # kanji -> [{jp,ko,tokens:[{surface,meaning}]}]
    yomi_dict = {}         # surface -> yomi (전역)
    max_ex_word = 0

    for kanji, d in words.items():
        idxs = sorted(d.keys(), key=lambda x: int(x) if x.isdigit() else 0)
        ex_list = []
        for i in idxs:
            tokens = [{"surface": m["surface"], "meaning": m["meaning"]}
                      for m in d[i]["morphemes"]]
            ex_list.append({"jp": d[i]["jp"], "ko": d[i]["ko"], "tokens": tokens})
            for m in d[i]["morphemes"]:
                yomi_dict.setdefault(m["surface"], m["yomi"])
        ex_doc[kanji] = ex_list
        max_ex_word = max(max_ex_word, jb({kanji: ex_list}))

    ex_b = jb(ex_doc)
    yomi_b = jb(yomi_dict)
    ex_ok = "✅ OK" if ex_b < ONE_MB else "❌ 초과"
    yo_ok = "✅ OK" if yomi_b < ONE_MB else "❌ 초과"
    print(f"{level:<5}{n:>6}  {ex_b:>15,}B {ex_b/ONE_MB:>5.2f}MB{ex_ok:>10}   "
          f"{yomi_b:>10,}B {yomi_b/ONE_MB:>5.2f}MB{yo_ok:>10}   "
          f"{ex_b//n if n else 0:>8}/{max_ex_word}B")

print(f"\n* Firestore 문서 1개 한계 = {ONE_MB:,}B (1MB)")
print("* 예문컬렉션: kanji→[{jp,ko,tokens:[{surface,meaning}]}]  (요미 제외, 문맥뜻 포함)")
print("* 요미사전:   surface→yomi  (전역 dedupe, 거의 불변)")
