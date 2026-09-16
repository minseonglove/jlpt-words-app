# -*- coding: utf-8 -*-
"""예문 후리가나 코어 — 형태소(surface, reading)를 한자 런 단위 루비로 정렬하고,
주석 형식(한자런[よみ], * 마커 유지)과 세그먼트 리스트를 상호 변환한다.

주석 형식 파서는 앱 스냅샷 생성기(TsvCatalog.parseFurigana)와 업로드 파서
(parsing.parse_furigana)에도 동일 규칙으로 포팅된다 — 규칙 변경 시 함께 수정.
"""
import re

# 々〆ヶ〇 는 요미가 선행 한자에 결합하므로 한자로 취급한다
KANJI_RE = re.compile(r"[㐀-鿿々〆ヶ〇]")
_KANJI_RUN = re.compile(r"[㐀-鿿々〆ヶ〇]+")


def kata_to_hira(s):
    return "".join(
        chr(ord(c) - 0x60) if "ァ" <= c <= "ヶ" else c
        for c in s
    )


def align_reading(surface, reading):
    """형태소 표면형과 요미를 한자 런 단위로 정렬한다.

    반환: [(text, ruby)] — ruby 는 한자 런에만 붙고 가나 구간은 "".
    정렬 실패(요미와 오쿠리가나 불일치)면 None.
    """
    reading = kata_to_hira(reading)
    if not KANJI_RE.search(surface):
        return [(surface, "")] if surface else []

    # 표면형을 한자 런 / 비한자 런으로 나눠 정규식을 만든다:
    # 한자 런 → (.+?) 캡처, 비한자 런 → 히라가나 변환 후 리터럴
    parts = []      # (text, is_kanji)
    pattern = ""
    pos = 0
    for m in _KANJI_RUN.finditer(surface):
        if m.start() > pos:
            kana = surface[pos:m.start()]
            parts.append((kana, False))
            pattern += re.escape(kata_to_hira(kana))
        parts.append((m.group(), True))
        pattern += "(.+?)"
        pos = m.end()
    if pos < len(surface):
        kana = surface[pos:]
        parts.append((kana, False))
        pattern += re.escape(kata_to_hira(kana))

    matched = re.fullmatch(pattern, reading)
    if not matched:
        return None
    result = []
    group_index = 1
    for text, is_kanji in parts:
        if is_kanji:
            result.append((text, matched.group(group_index)))
            group_index += 1
        else:
            result.append((text, ""))
    return result


class _MarkerBag:
    """같은 오프셋에 여러 마커(*x**y* 인접)가 올 수 있어 multiset 으로 관리."""

    def __init__(self, offsets):
        self._counts = {}
        for o in offsets:
            self._counts[o] = self._counts.get(o, 0) + 1

    def __contains__(self, offset):
        return self._counts.get(offset, 0) > 0

    def remove_first(self, offset):
        self._counts[offset] -= 1

    def offsets(self):
        return [o for o, c in self._counts.items() for _ in range(c)]


def _marker_positions(sentence_with_markers):
    offsets = []
    stripped_pos = 0
    for ch in sentence_with_markers:
        if ch == "*":
            offsets.append(stripped_pos)
        else:
            stripped_pos += 1
    return _MarkerBag(offsets)


def segments_to_annotated(sentence_with_markers, segments):
    """마커 제거 문장을 덮는 세그먼트를 주석 형식으로 되돌린다(마커 위치 유지).

    세그먼트 text 연결이 마커 제거 문장과 다르거나, 마커 경계가 세그먼트 중간에
    떨어지면 ValueError — 생성기가 경계 분할을 보장해야 한다.
    """
    stripped = sentence_with_markers.replace("*", "")
    if "".join(t for t, _ in segments) != stripped:
        raise ValueError(f"세그먼트 연결 불일치: {stripped!r}")
    boundaries = {0}
    offset = 0
    for text, _ in segments:
        offset += len(text)
        boundaries.add(offset)
    marker_bag = _marker_positions(sentence_with_markers)
    for mo in marker_bag.offsets():
        if mo not in boundaries:
            raise ValueError(f"마커가 세그먼트 중간에 위치: offset={mo}")
    out = []
    offset = 0
    for text, ruby in segments:
        while offset in marker_bag:
            out.append("*")
            marker_bag.remove_first(offset)
        out.append(f"{text}[{ruby}]" if ruby else text)
        offset += len(text)
    while offset in marker_bag:
        out.append("*")
        marker_bag.remove_first(offset)
    return "".join(out)


def annotated_strip(annotated):
    """주석에서 [よみ] 만 제거 — 마커 포함 원문 복원."""
    return re.sub(r"\[[^\]]*\]", "", annotated)


def annotated_to_segments(annotated):
    """주석 → (text, ruby) 세그먼트. 마커는 제거하되 경계에서 분할한다.

    인접한 무루비 구간은 병합한다(경계가 다른 곳 제외). 루비 구간은 단독 세그먼트.
    """
    segments = []
    plain = ""

    def flush():
        nonlocal plain
        if plain:
            segments.append((plain, ""))
            plain = ""

    i = 0
    while i < len(annotated):
        ch = annotated[i]
        if ch == "*":
            flush()
            i += 1
            continue
        if ch == "[":
            end = annotated.index("]", i)
            ruby = annotated[i + 1:end]
            # 직전 한자 런이 루비의 본문이다
            m = None
            for m in _KANJI_RUN.finditer(plain):
                pass
            if m is None or m.end() != len(plain):
                raise ValueError(f"루비 앞에 한자 런이 없음: {annotated!r}")
            base = m.group()
            plain = plain[: m.start()]
            flush()
            segments.append((base, ruby))
            i = end + 1
            continue
        plain += ch
        i += 1
    flush()
    return segments
