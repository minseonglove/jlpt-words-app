#!/usr/bin/env python3
"""
레벨별로 all_word_meanings.tsv의 example_jp에서 source_word 활용형을 *...*로 마킹.
Usage: python3 highlight_level.py <level>  (n1–n5)
"""
import json
import os
import sys
import time
from pathlib import Path

import anthropic


def _load_env_file() -> None:
    """ANTHROPIC_API_KEY가 없으면 scripts/.env 에서 로드."""
    if os.environ.get("ANTHROPIC_API_KEY"):
        return
    env_file = Path(__file__).parent / ".env"
    if not env_file.exists():
        return
    with open(env_file) as f:
        for line in f:
            line = line.strip()
            if line.startswith("ANTHROPIC_API_KEY="):
                key = line.split("=", 1)[1].strip().strip("'\"")
                os.environ["ANTHROPIC_API_KEY"] = key
                return

# ──────────────────────────────────────────────

BATCH_SIZE = 30
BASE       = Path(__file__).parent
TSV_PATH   = BASE / "meaning_gen" / "all_word_meanings.tsv"

def _paths(level: str):
    return (
        BASE / f"highlight_ckpt_{level}.json",
        BASE / f"highlight_results_{level}.json",
        BASE / f"highlight_unmatched_{level}.jsonl",
    )

def _base_form(source_word: str) -> str:
    """'望む(のぞむ)' → '望む'"""
    idx = source_word.find("(")
    return source_word[:idx].strip() if idx != -1 else source_word.strip()

# ──────────────────────────────────────────────

PROMPT_TEMPLATE = """\
以下の各項目について、sentenceの中でwordがどのような活用形・形で現れているか特定し、その部分だけを*...*で囲んでください。

ルール:
- wordの活用形（て形・た形・連用形・否定形・受け身形・連体形など）を正確に特定すること
- *マーカーは活用形全体を囲む（語幹だけ・一部だけはNG）
- sentenceにwordが見当たらない場合はsentenceをそのまま返す（*なし）
- LLMの裁量で、その文脈で最も意味的に対応する箇所を1か所だけマーク

例:
- word=望む, sentence=誰もが平和な生活を望んでいる。 → 誰もが平和な生活を*望んで*いる。
- word=望む, sentence=丘の上から海を望む景色は絶景だった。 → 丘の上から海を*望む*景色は絶景だった。
- word=美しい, sentence=美しい景色に感動した。 → *美しい*景色に感動した。
- word=走る, sentence=公園を走っていた。 → 公園を*走って*いた。

入力 (JSON):
{items_json}

JSONのみ出力（マークダウン・説明文不要）:
[{{"id": 0, "marked": "マーク済み文字列"}}, ...]"""


def _call_llm(client: anthropic.Anthropic, batch: list[tuple]) -> list[dict]:
    """batch: [(key, base_form, example_jp), ...] → [{"key":..., "marked":...}, ...]"""
    items = [
        {"id": i, "word": bf, "sentence": ex}
        for i, (_, bf, ex) in enumerate(batch)
    ]
    prompt = PROMPT_TEMPLATE.format(items_json=json.dumps(items, ensure_ascii=False))

    for attempt in range(3):
        try:
            resp = client.messages.create(
                model="claude-haiku-4-5-20251001",
                max_tokens=8192,
                messages=[{"role": "user", "content": prompt}],
            )
            text = resp.content[0].text.strip()
            if text.startswith("```"):
                text = "\n".join(l for l in text.split("\n") if not l.startswith("```"))
            results = json.loads(text.strip())
            return [{"key": batch[r["id"]][0], "marked": r["marked"]} for r in results]
        except anthropic.RateLimitError:
            wait = 60 * (attempt + 1)
            print(f"    레이트 리밋 — {wait}초 대기…", file=sys.stderr, flush=True)
            time.sleep(wait)
        except Exception as exc:
            print(f"    시도 {attempt+1}/3 실패: {exc}", file=sys.stderr, flush=True)
            if attempt < 2:
                time.sleep(3 * (attempt + 1))

    print("    폴백: 원문 그대로 반환", file=sys.stderr, flush=True)
    return [{"key": k, "marked": ex} for k, _, ex in batch]


# ──────────────────────────────────────────────

def main() -> None:
    _load_env_file()

    if len(sys.argv) < 2 or sys.argv[1] not in {"n1", "n2", "n3", "n4", "n5"}:
        sys.exit("Usage: python3 highlight_level.py <level>  (n1–n5)")

    level = sys.argv[1]
    ckpt_path, out_path, unmatch_path = _paths(level)

    # ── 1. Unique (source_word, example_jp) 쌍 수집 ──────────────
    pairs: dict[str, tuple[str, str]] = {}
    with open(TSV_PATH, encoding="utf-8") as f:
        for line in f:
            cols = line.rstrip("\n").split("\t")
            if len(cols) < 9 or cols[0] != level:
                continue
            sw, ex = cols[1], cols[3]
            key = f"{sw}|||{ex}"
            if key not in pairs:
                pairs[key] = (_base_form(sw), ex)

    print(f"[{level}] unique 쌍: {len(pairs)}개", flush=True)

    # ── 2. 체크포인트 로드 ─────────────────────────────────────────
    done: dict[str, str] = {}
    if ckpt_path.exists():
        with open(ckpt_path, encoding="utf-8") as f:
            done = json.load(f)
        print(f"[{level}] 체크포인트 재개: {len(done)}/{len(pairs)}", flush=True)

    todo = [(k, bf, ex) for k, (bf, ex) in pairs.items() if k not in done]
    n_batches = (len(todo) + BATCH_SIZE - 1) // BATCH_SIZE

    # ── 3. LLM 배치 처리 ──────────────────────────────────────────
    if todo:
        client = anthropic.Anthropic()
        for bi in range(0, len(todo), BATCH_SIZE):
            batch = todo[bi : bi + BATCH_SIZE]
            bn = bi // BATCH_SIZE + 1
            print(f"  [{level}] 배치 {bn}/{n_batches} ({len(batch)}쌍)…", flush=True)

            results = _call_llm(client, batch)
            for r in results:
                done[r["key"]] = r["marked"]

            with open(ckpt_path, "w", encoding="utf-8") as f:
                json.dump(done, f, ensure_ascii=False)

            time.sleep(0.3)

    # ── 4. 미매칭 분류 & 저장 ────────────────────────────────────
    unmatched = []
    for key, marked in done.items():
        if "*" not in marked:
            sw_part, ex_part = key.split("|||", 1)
            unmatched.append({"level": level, "source_word": sw_part, "example_jp": ex_part})

    with open(unmatch_path, "w", encoding="utf-8") as f:
        for row in unmatched:
            f.write(json.dumps(row, ensure_ascii=False) + "\n")

    print(f"[{level}] 미매칭: {len(unmatched)}건 → {unmatch_path.name}", flush=True)

    # ── 5. 최종 결과 저장 ─────────────────────────────────────────
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(done, f, ensure_ascii=False, indent=2)
    print(f"[{level}] 완료 — {len(done)}건 저장 → {out_path.name}", flush=True)

    if ckpt_path.exists():
        ckpt_path.unlink()


if __name__ == "__main__":
    main()
