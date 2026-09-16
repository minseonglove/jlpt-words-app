# -*- coding: utf-8 -*-
import pytest
from generate_furigana import annotate_sentence, make_tagger


@pytest.fixture(scope="module")
def tagger():
    return make_tagger()


def test_basic_sentence(tagger):
    annotated, flags = annotate_sentence(tagger, "学校へ*行き*ます。", [])
    assert annotated == "学校[がっこう]へ*行[い]き*ます。"
    assert flags == []


def test_curated_token_overrides_mecab(tagger):
    # 토큰 발음(문맥 검수 값)이 MeCab 결과와 다르면 토큰을 우선한다
    # MeCab 은 단독 "一日" 를 기본값 "ついたち"(매월 1일)로 읽는다 — "いちにち"(하루 종일)
    # 문맥 큐레이션 값으로 덮어써야 하는 실제 사례.
    annotated, flags = annotate_sentence(
        tagger, "*一日*は家にいました。", [("一日", "いちにち")]
    )
    assert annotated.startswith("*一日[いちにち]*")
    assert "家[いえ]" in annotated or "家[うち]" in annotated


def test_no_kanji_sentence(tagger):
    annotated, flags = annotate_sentence(tagger, "*もしもし*、どうも。", [])
    assert annotated == "*もしもし*、どうも。"
    assert flags == []


def test_roundtrip_matches_source(tagger):
    src = "彼は*まだ*寝ています。"
    annotated, _ = annotate_sentence(tagger, src, [])
    from furigana_core import annotated_strip
    assert annotated_strip(annotated) == src


def test_marker_cross_partial_preserves_rest_of_annotation(tagger):
    # 마커가 루비 세그먼트(MeCab 이 "日中" 를 한 단어 にっちゅう 로 묶음) 중간에 떨어져도,
    # 문장 전체를 무루비 폴백시키지 않고 그 세그먼트만 검수 대상(marker_cross)으로 남긴 채
    # 나머지 주석은 보존해야 한다. (실 코퍼스 n5/一日/0 과 동일한 문장)
    annotated, flags = annotate_sentence(
        tagger, "*一日*中家にいました。", [("一日", "いちにち")]
    )
    assert annotated == "*一[いち]日*中家[か]にいました。"
    assert ("marker_cross", "2") in flags
    assert ("marker_cross", "fallback_plain") not in flags


def test_replace_span_partial_overlap_uses_intersecting_substring_only():
    from generate_furigana import _replace_span

    # segments: "たかし"(무루비, 3자) + "川"(루비 かわ, 1자) — 전체 "たかし川"
    # span[1:4) 은 "かし川" 만 덮는다(좌측 "た" 는 스팬 밖). 실제 스팬 내용의 정확한
    # 요미는 "かしかわ" 다.
    segments = [("たかし", ""), ("川", "かわ")]

    # case 1 — align_reading 자체가 실패하는 경로 검증 (covered 트리밍 회귀 가드는 아님).
    # token_surface="かし川" 의 정렬 패턴은 "かし" 로 시작해야 하는데 expected_reading="たかしかわ"
    # 는 "た" 로 시작해 애초에 정렬이 안 된다. `_replace_span` 은 aligned가 None이면 comparable
    # 값(트리밍 여부)과 무관하게 항상 None 을 반환하므로, 구버전(오염된 comparable="たかしかわ")도
    # 이 case 에서는 우연히 동일하게 None 을 반환한다 — covered 트리밍 여부를 판별하지 못한다.
    # 실제 트리밍 회귀 가드는 아래 case 2.
    result = _replace_span(
        segments, span_start=1, span_len=3,
        token_surface="かし川", expected_reading="たかしかわ",
    )
    assert result is None

    # case 2 — comparable 오염 회귀 가드. 트리밍된 실제 내용("かしかわ")과 정확히 일치하면
    # 변경 없음으로 처리되어야 한다. 구버전처럼 covered 에 스팬 밖 "た" 까지 포함시키면
    # comparable 이 "たかしかわ" 로 오염되어 이 expected_reading("かしかわ")과 불일치 판정,
    # align_reading 이 성공(패턴이 "かし" 로 시작해 매칭됨)해 불필요한 오버라이드가 조용히
    # 적용되는 회귀가 발생한다 — 이 assert 가 그 회귀를 잡는다.
    result_match = _replace_span(
        segments, span_start=1, span_len=3,
        token_surface="かし川", expected_reading="かしかわ",
    )
    assert result_match == (segments, False)

    # case 3 — 트리밍된 실제 내용과 다르면(정상적 오버라이드 필요) 겹치는 부분만 정확히 교체된다
    result_changed = _replace_span(
        segments, span_start=1, span_len=3,
        token_surface="かし川", expected_reading="かしがわ",
    )
    assert result_changed == ([("た", ""), ("かし", ""), ("川", "がわ")], True)


def test_load_overrides_duplicate_key_raises(tmp_path, monkeypatch):
    # append 로 같은 (level, source_word, meaning_index) 행이 중복되면 last-wins 로
    # 조용히 덮어쓰지 않고 즉시 실패해야 한다 — 기존 행은 직접 수정해야 하기 때문.
    import generate_furigana

    overrides_file = tmp_path / "furigana_overrides.tsv"
    overrides_file.write_text(
        "level\tsource_word\tmeaning_index\tannotated\n"
        "n5\t食べる\t0\tご飯[はん]を*食[た]べ*ます。\n"
        "n5\t食べる\t0\tご飯[はん]を*食[た]べ*ました。\n",
        encoding="utf-8",
    )
    monkeypatch.setattr(generate_furigana, "OVERRIDES_TSV", str(overrides_file))

    with pytest.raises(SystemExit):
        generate_furigana._load_overrides()
