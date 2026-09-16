# -*- coding: utf-8 -*-
"""all_word_meanings.tsv 예문 전체의 후리가나 주석 TSV 를 생성한다.

우선순위: overrides > 예문 귀속 큐레이션 토큰(vocab_pron) > MeCab(UniDic).
사용법: python3 generate_furigana.py
산출물: ../meaning_gen/example_furigana.tsv, furigana_flags.tsv
"""
import csv
import os

import fugashi

from furigana_core import (
    KANJI_RE,
    align_reading,
    annotated_strip,
    kata_to_hira,
    segments_to_annotated,
)

HERE = os.path.dirname(os.path.abspath(__file__))
MEANINGS_TSV = os.path.join(HERE, "..", "meaning_gen", "all_word_meanings.tsv")
OUTPUT_TSV = os.path.join(HERE, "..", "meaning_gen", "example_furigana.tsv")
OVERRIDES_TSV = os.path.join(HERE, "furigana_overrides.tsv")
FLAGS_TSV = os.path.join(HERE, "furigana_flags.tsv")


def make_tagger():
    return fugashi.Tagger()


def _morpheme_reading(word):
    """UniDic kana(표면형 요미, 카타카나) → 히라가나. 없으면 None."""
    kana = getattr(word.feature, "kana", None)
    if not kana or kana == "*":
        return None
    return kata_to_hira(kana)


def _mecab_segments(tagger, stripped):
    """마커 제거 문장 → (text, ruby) 세그먼트 + flags. 문장 전체를 덮는다."""
    segments = []
    flags = []
    for word in tagger(stripped):
        surface = word.surface
        if not surface:
            continue
        if not KANJI_RE.search(surface):
            segments.append((surface, ""))
            continue
        reading = _morpheme_reading(word)
        if reading is None:
            segments.append((surface, ""))
            flags.append(("unknown_reading", surface))
            continue
        aligned = align_reading(surface, reading)
        if aligned is None:
            # 정렬 실패 — 형태소 전체 루비로 폴백하고 검수 대상으로 남긴다
            segments.append((surface, reading))
            flags.append(("align_failed", surface))
            continue
        segments.extend(aligned)
    return segments, flags


def _apply_curated_tokens(stripped, segments, curated_tokens):
    """예문 귀속 토큰(vocab_word→vocab_pron)이 문장에 그대로 나타나면 그 구간 요미를 토큰 값으로 교체."""
    flags = []
    for token_surface, token_reading in sorted(curated_tokens, key=lambda t: -len(t[0])):
        if not KANJI_RE.search(token_surface):
            continue
        expected = kata_to_hira(token_reading)
        start = 0
        while True:
            idx = stripped.find(token_surface, start)
            if idx < 0:
                break
            start = idx + len(token_surface)  # 자기 겹침 재매칭 방지 (예: "ああ" in "あああ")
            replaced = _replace_span(segments, idx, len(token_surface), token_surface, expected)
            if replaced is None:
                flags.append(("token_align_failed", token_surface))
            elif replaced:
                segments[:] = replaced[0]
                if replaced[1]:
                    flags.append(("token_mismatch_applied", token_surface))
    return flags


def _replace_span(segments, span_start, span_len, token_surface, expected_reading):
    """세그먼트 리스트에서 [span_start, span_start+span_len) 구간을 토큰 정렬 결과로 교체.

    반환: None=교체 불가(경계가 루비 세그먼트 중간), (new_segments, changed)=성공.
    구간의 기존 요미가 이미 expected 와 같으면 changed=False 로 무변경.
    """
    span_end = span_start + span_len
    new_segments = []
    covered = []          # 구간에 완전히 포함되는 세그먼트들
    offset = 0
    insert_at = None
    for text, ruby in segments:
        seg_start, seg_end = offset, offset + len(text)
        offset = seg_end
        if seg_end <= span_start or seg_start >= span_end:
            new_segments.append((text, ruby))
            continue
        # 부분 겹침 — 무루비 세그먼트는 잘라내고, 루비 세그먼트면 교체 불가
        if seg_start < span_start or seg_end > span_end:
            if ruby:
                return None
            if seg_start < span_start:
                new_segments.append((text[: span_start - seg_start], ""))
            if insert_at is None:
                insert_at = len(new_segments)
            # covered 에는 스팬과 실제로 겹치는 부분 문자열만 넣는다 — 전체 text 를 넣으면
            # comparable 이 오염되어 우연히 expected_reading 과 일치, 오버라이드가 플래그 없이
            # 조용히 버려질 수 있다.
            inter_start = max(0, span_start - seg_start)
            inter_end = min(len(text), span_end - seg_start)
            covered.append((text[inter_start:inter_end], ""))
            if seg_end > span_end:
                new_segments.append((text[span_end - seg_start:], ""))
            continue
        if insert_at is None:
            insert_at = len(new_segments)
        covered.append((text, ruby))

    # 구간 내 가나 본문도 발음의 일부다 — 비교용 요미는 (루비 or 가나본문) 연결
    comparable = "".join(ruby if ruby else kata_to_hira(t) for t, ruby in covered)
    aligned = align_reading(token_surface, expected_reading)
    if aligned is None:
        return None
    if comparable == expected_reading:
        return ([s for s in segments], False)
    new_segments[insert_at:insert_at] = aligned
    return (new_segments, True)


def annotate_sentence(tagger, sentence_with_markers, curated_tokens):
    """예문 1건 주석. curated_tokens: [(vocab_word, vocab_pron)]."""
    stripped = sentence_with_markers.replace("*", "")
    segments, flags = _mecab_segments(tagger, stripped)
    flags += _apply_curated_tokens(stripped, segments, curated_tokens)

    # 마커 경계에서 세그먼트 분할 (루비 중간이면 그 세그먼트만 루비를 버리고 분할 — flag 로 검수 대상 표시)
    marker_offsets = []
    pos = 0
    for ch in sentence_with_markers:
        if ch == "*":
            marker_offsets.append(pos)
        else:
            pos += 1
    for mo in marker_offsets:
        segments, ok = _split_at(segments, mo)
        if not ok:
            flags.append(("marker_cross", str(mo)))
    try:
        annotated = segments_to_annotated(sentence_with_markers, segments)
    except ValueError:
        # 그래도 경계가 맞지 않는 경우(드묾) — 루비 전체 포기, 원문 그대로 (검수 대상)
        annotated = sentence_with_markers
        flags.append(("marker_cross", "fallback_plain"))
    return annotated, flags


def _split_at(segments, offset):
    """세그먼트를 offset 에서 분할한다.

    루비 세그먼트 중간에 offset 이 떨어지면 그 세그먼트만 루비를 버려 (text, "") 로
    강등한 뒤 분할한다 — 문장 전체를 무루비 폴백시키지 않고 그 한 단어만 검수 대상으로
    남기면서 나머지 주석은 보존하기 위함. ok=False 는 그 사실을 flag 로 남기라는 신호.
    """
    out = []
    pos = 0
    ok = True
    for text, ruby in segments:
        start, end = pos, pos + len(text)
        pos = end
        if start < offset < end:
            if ruby:
                ok = False
            out.append((text[: offset - start], ""))
            out.append((text[offset - start:], ""))
        else:
            out.append((text, ruby))
    return out, ok


def _load_examples():
    """{(level, source_word, midx): (sentence, [(vocab, pron)])} — 소스 등장 순."""
    examples = {}
    with open(MEANINGS_TSV, encoding="utf-8", newline="") as f:
        for row in csv.reader(f, delimiter="\t"):
            if len(row) < 9:
                continue
            level, source_word, _, jp, _, midx, vocab, vpron, _ = row[:9]
            key = (level, source_word, midx.strip())
            if key not in examples:
                examples[key] = (jp, [])
            examples[key][1].append((vocab, vpron))
    return examples


def _load_overrides():
    overrides = {}
    if not os.path.exists(OVERRIDES_TSV):
        return overrides
    with open(OVERRIDES_TSV, encoding="utf-8", newline="") as f:
        for row in csv.reader(f, delimiter="\t"):
            if len(row) < 4 or row[0] == "level":
                continue
            key = (row[0], row[1], row[2])
            if key in overrides:
                raise SystemExit(
                    f"중복 override 키: {key} — "
                    "furigana_overrides.tsv 에서 기존 행을 직접 수정하세요 (append 금지)"
                )
            overrides[key] = row[3]
    return overrides


def main():
    tagger = make_tagger()
    examples = _load_examples()
    overrides = _load_overrides()

    out_rows = []
    flag_rows = []
    for (level, source_word, midx), (jp, tokens) in examples.items():
        override = overrides.get((level, source_word, midx))
        if override is not None:
            if annotated_strip(override) != jp:
                raise SystemExit(
                    f"override 원문 불일치: {level}/{source_word}/{midx}\n{override}\n{jp}"
                )
            out_rows.append((level, source_word, midx, override))
            continue
        annotated, flags = annotate_sentence(tagger, jp, tokens)
        if annotated_strip(annotated) != jp:
            # 코어 불변식(원문 복원) 위반 — 이 예문만 원문 그대로 폴백하고 검수 대상으로 남긴다.
            # 전체 배치를 죽이지 않는다.
            annotated = jp
            flags = flags + [("marker_cross", "corpus_assert_failed")]
        out_rows.append((level, source_word, midx, annotated))
        for flag, detail in flags:
            flag_rows.append((level, source_word, midx, f"{flag}:{detail}", annotated))

    with open(OUTPUT_TSV, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, delimiter="\t", lineterminator="\n")
        w.writerows(out_rows)
    with open(FLAGS_TSV, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, delimiter="\t", lineterminator="\n")
        w.writerows(flag_rows)
    print(f"annotated={len(out_rows)} flags={len(flag_rows)} overrides={len(overrides)}")


if __name__ == "__main__":
    main()
