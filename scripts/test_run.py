#!/usr/bin/env python3
"""테스트: 처음 5개 단어만 처리"""

import sys
sys.path.insert(0, "/Users/gimminseong/jlpt-words/scripts")

from generate_examples import parse_csv, call_thinking_agent, call_writing_agent
from pathlib import Path

INPUT_DIR = Path("/Users/gimminseong/jlpt-words/data-kmp/src/androidMain/res/raw")

# N1 처음 5개만 테스트
words = parse_csv(INPUT_DIR / "n1words_shuffled.csv")[:5]

print("=== 입력 단어 ===")
for w in words:
    print(f"  {w['num']}. {w['word']} ({w['reading']}) - {w['meaning_raw']} [{w['pos']}]")

print("\n=== 1단계: 생각 에이전트 ===")
thinking = call_thinking_agent(words, "N1")
print(thinking[:500] + "..." if len(thinking) > 500 else thinking)

print("\n=== 2단계: 작성 에이전트 ===")
tsv = call_writing_agent(words, thinking, 1)
print(tsv)

print("\n=== 탭 구분 확인 ===")
for line in tsv.strip().split("\n"):
    cols = line.split("\t")
    print(f"  열 개수: {len(cols)}")
    for i, col in enumerate(cols):
        labels = ["번호", "단어", "발음", "뜻", "품사", "예문", "예문의뜻", "예문에 쓰인 단어"]
        label = labels[i] if i < len(labels) else f"열{i}"
        print(f"    [{label}]: {col[:60]}")
