# -*- coding: utf-8 -*-
"""example_blob 인코딩/압축/분할 검증.

protobuf 바이트 레이아웃은 손으로 계산한 기대값과 대조한다.
앱(Wire) 쪽 디코드와의 교차 검증은 data-kmp 의 ExampleChunkCodecTest(골든 blob)가 담당한다.
"""
import zlib

import example_blob


def _inflate_raw(blob):
    return zlib.decompress(blob, -zlib.MAX_WBITS)


def test_token_encoding_known_vector():
    # ChunkToken {surface="a"(1), meaning="b"(2)}
    # 필드1 tag=0x0A, len=1, 'a' / 필드2 tag=0x12, len=1, 'b'
    assert example_blob._token({"surface": "a", "meaning": "b"}) == b"\x0a\x01a\x12\x01b"


def test_empty_string_field_is_omitted():
    # proto3 기본값(빈 문자열)은 생략 — 디코더는 "" 로 복원한다
    assert example_blob._token({"surface": "a", "meaning": ""}) == b"\x0a\x01a"


def test_chunk_encoding_structure():
    items = [
        (
            "食べる",
            [{"jp": "ご飯を食べます。", "ko": "밥을 먹습니다.", "tokens": [{"surface": "ご飯", "meaning": "밥"}]}],
        )
    ]
    raw = example_blob.encode_chunk(items)
    # ExampleChunk.words(1) → tag 0x0A 로 시작
    assert raw[0] == 0x0A
    # UTF-8 본문이 그대로 포함된다
    assert "食べる".encode("utf-8") in raw
    assert "밥을 먹습니다.".encode("utf-8") in raw


def test_compress_roundtrip_is_raw_deflate():
    items = [("水", [{"jp": "水を飲む。", "ko": "물을 마신다.", "tokens": []}])]
    raw = example_blob.encode_chunk(items)
    blob = example_blob.compress(raw)
    assert _inflate_raw(blob) == raw
    # zlib 헤더(0x78)가 없어야 raw deflate — iOS NSData zlib 과 호환 조건
    assert blob[:1] != b"\x78"


def test_build_blobs_single_when_small():
    ex = {"水": [{"jp": "水を飲む。", "ko": "물을 마신다.", "tokens": []}]}
    blobs = example_blob.build_blobs(ex)
    assert len(blobs) == 1


def test_build_blobs_splits_over_max_bytes():
    # 반복 문자열은 압축돼 한도를 안 넘으므로, 해시 기반의 결정적 저압축 데이터로 초과를 유도
    import hashlib

    def noise(i):
        return "".join(hashlib.sha256(f"{i}-{j}".encode()).hexdigest() for j in range(20))

    ex = {f"単語{i}": [{"jp": noise(i), "ko": noise(i + 100), "tokens": []}] for i in range(8)}
    blobs = example_blob.build_blobs(ex, max_bytes=1000)
    assert len(blobs) > 1
    # 분할된 blob 을 모두 풀면 전체 항목이 보존된다
    joined = b"".join(_inflate_raw(b) for b in blobs)
    full = example_blob.encode_chunk(sorted(ex.items(), key=lambda kv: kv[0]))
    assert joined == full


def test_build_blobs_deterministic_order():
    ex = {"b": [{"jp": "x", "ko": "y", "tokens": []}], "a": [{"jp": "x", "ko": "y", "tokens": []}]}
    assert example_blob.build_blobs(ex) == example_blob.build_blobs(dict(reversed(list(ex.items()))))


def test_readings_blob_roundtrip():
    readings = {"食べる": "たべる", "水": "みず"}
    blob = example_blob.build_readings_blob(readings)
    raw = _inflate_raw(blob)
    # ReadingEntry {surface="水"(1), reading="みず"(2)} — surface 정렬로 水 가 먼저
    mizu = b"\x0a\x03" + "水".encode() + b"\x12\x06" + "みず".encode()
    assert raw.startswith(b"\x0a" + bytes([len(mizu)]) + mizu)


def test_readings_blob_over_limit_raises():
    import hashlib
    import pytest

    readings = {hashlib.sha256(str(i).encode()).hexdigest(): str(i) for i in range(100)}
    with pytest.raises(ValueError):
        example_blob.build_readings_blob(readings, max_bytes=100)


def test_ruby_encoding_known_vector():
    # ChunkRuby {text="水"(1), reading="みず"(2)}
    # 필드1 tag=0x0A, len=3(UTF-8), '水' / 필드2 tag=0x12, len=6, 'みず'
    assert example_blob._ruby(("水", "みず")) == b"\x0a\x03" + "水".encode() + b"\x12\x06" + "みず".encode()


def test_ruby_encoding_omits_empty_reading():
    # 가나 세그먼트(reading="")는 proto3 빈 문자열 생략 규칙에 따라 필드2 를 내보내지 않는다
    assert example_blob._ruby(("を", "")) == b"\x0a\x03" + "を".encode()


def test_furigana_field_roundtrip():
    example = {
        "jp": "水を飲む。",
        "ko": "물을 마신다.",
        "tokens": [{"surface": "水", "meaning": "물"}],
        "furigana": [("水", "みず"), ("を", ""), ("飲", "の"), ("む。", "")],
    }
    encoded = example_blob._example(example)
    without_furigana = example_blob._example(dict(example, furigana=[]))
    # furigana 세그먼트가 있으면 인코딩이 달라져야 한다
    assert encoded != without_furigana
    assert encoded.startswith(without_furigana)
    # field 4 (wire type 2) 메시지 헤더가 세그먼트 수(4개)만큼 정확히 등장해야 한다 —
    # 손으로 디코딩해 각 세그먼트를 재구성한다 (0x22 바이트 카운트는 본문 UTF-8 과
    # 우연히 겹칠 수 있어 취약하므로 사용하지 않는다).
    tail = encoded[len(without_furigana):]
    decoded_segments = []
    i = 0
    while i < len(tail):
        assert tail[i] == 0x22  # (4 << 3) | 2
        i += 1
        length = tail[i]
        i += 1
        payload = tail[i : i + length]
        i += length
        # payload 자체를 ChunkRuby {text=1, reading=2} 로 디코딩
        j = 0
        text = ""
        reading = ""
        while j < len(payload):
            tag = payload[j]
            j += 1
            flen = payload[j]
            j += 1
            value = payload[j : j + flen].decode("utf-8")
            j += flen
            if tag == 0x0A:
                text = value
            elif tag == 0x12:
                reading = value
        decoded_segments.append((text, reading))
    assert decoded_segments == example["furigana"]


def test_furigana_absent_keeps_encoding_identical():
    example = {"jp": "水を飲む。", "ko": "물을 마신다.", "tokens": []}
    with_key = dict(example, furigana=[])
    assert example_blob._example(example) == example_blob._example(with_key)
