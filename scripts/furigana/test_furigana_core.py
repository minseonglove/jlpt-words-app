# -*- coding: utf-8 -*-
from furigana_core import (
    align_reading,
    annotated_strip,
    annotated_to_segments,
    kata_to_hira,
    segments_to_annotated,
)


def test_kata_to_hira():
    assert kata_to_hira("タベマス") == "たべます"
    assert kata_to_hira("ページ") == "ぺーじ"  # 장음 유지
    assert kata_to_hira("たなか") == "たなか"


def test_align_simple_noun():
    assert align_reading("田中", "たなか") == [("田中", "たなか")]


def test_align_okurigana_suffix():
    assert align_reading("食べます", "たべます") == [("食", "た"), ("べます", "")]


def test_align_interleaved():
    assert align_reading("取り引き", "とりひき") == [("取", "と"), ("り", ""), ("引", "ひ"), ("き", "")]


def test_align_kana_prefix():
    assert align_reading("お茶", "おちゃ") == [("お", ""), ("茶", "ちゃ")]


def test_align_katakana_reading_input():
    # UniDic kana 는 카타카나 — 내부에서 히라가나로 변환된다
    assert align_reading("食べた", "タベタ") == [("食", "た"), ("べた", "")]


def test_align_no_kanji():
    assert align_reading("です", "です") == [("です", "")]


def test_align_mismatch_returns_none():
    # 오쿠리가나가 요미와 대응하지 않으면 정렬 실패
    assert align_reading("食べる", "のむ") is None


def test_align_repetition_mark_as_kanji():
    assert align_reading("人々", "ひとびと") == [("人々", "ひとびと")]


def test_annotated_roundtrip_with_markers():
    sentence = "授業は九時に*始まり*ます。"
    segments = [
        ("授業", "じゅぎょう"), ("は", ""), ("九時", "くじ"), ("に", ""),
        ("始", "はじ"), ("まり", ""), ("ます。", ""),
    ]
    annotated = segments_to_annotated(sentence, segments)
    assert annotated == "授業[じゅぎょう]は九時[くじ]に*始[はじ]まり*ます。"
    assert annotated_strip(annotated) == sentence
    # 마커 경계에서 분할되고 마커는 제거된다. 인접 무루비 구간은 병합.
    assert annotated_to_segments(annotated) == [
        ("授業", "じゅぎょう"), ("は", ""), ("九時", "くじ"), ("に", ""),
        ("始", "はじ"), ("まり", ""), ("ます。", ""),
    ]


def test_annotated_to_segments_splits_at_marker_boundary():
    # 마커 안팎에 걸치는 무루비 구간은 경계에서 분할되어야 한다
    annotated = "リンゴを*一[ひと]つ*ください。"
    assert annotated_to_segments(annotated) == [
        ("リンゴを", ""), ("一", "ひと"), ("つ", ""), ("ください。", ""),
    ]


def test_segments_to_annotated_rejects_length_mismatch():
    import pytest
    with pytest.raises(ValueError):
        segments_to_annotated("田中です。", [("田中", "たなか")])
