# -*- coding: utf-8 -*-
"""콘텐츠 blob 인코딩 — protobuf 직렬화 + raw deflate 압축.

- 예문 청크: ExampleChunk (data-kmp/src/commonMain/proto/ExampleChunk.proto)
- 요미 사전: ReadingsBlob (data-kmp/src/commonMain/proto/Readings.proto)

protoc 의존성을 피하려고 필요한 인코딩(length-delimited 필드)만 손으로 구현했다.
스키마를 바꾸면 proto 파일과 이 인코더를 함께 수정하고,
data-kmp 의 ExampleChunkCodecTest / ReadingsBlobCodecTest 골든 blob 도 재생성해야 한다.

앱 쪽 해제는 raw deflate(zlib 헤더 없음) 기준이다 — iOS NSData 의 zlib
알고리즘이 raw deflate 라서 세 플랫폼 공통 형식으로 raw 를 쓴다.
"""
import zlib

# Firestore 문서 한도 1MiB 의 안전 마진 (필드명·문서 오버헤드 여유)
MAX_BLOB_BYTES = 900_000


def _varint(n):
    out = bytearray()
    while True:
        b = n & 0x7F
        n >>= 7
        if n:
            out.append(b | 0x80)
        else:
            out.append(b)
            return bytes(out)


def _message(field_no, payload):
    """내장 메시지 필드(wire type 2). repeated 항목 수 자체가 의미라 빈 payload 도 내보낸다."""
    return _varint((field_no << 3) | 2) + _varint(len(payload)) + payload


def _string(field_no, s):
    """proto3 string 필드. 빈 문자열은 기본값이므로 생략한다."""
    if not s:
        return b""
    return _message(field_no, s.encode("utf-8"))


def _token(token):
    """ChunkToken {surface=1, meaning=2}"""
    return _string(1, token["surface"]) + _string(2, token["meaning"])


def _ruby(segment):
    """ChunkRuby {text=1, reading=2}"""
    text, reading = segment
    return _string(1, text) + _string(2, reading)


def _example(example):
    """ChunkExample {japanese=1, korean=2, tokens=3, furigana=4}"""
    body = _string(1, example["jp"]) + _string(2, example["ko"])
    for token in example["tokens"]:
        body += _message(3, _token(token))
    for segment in example.get("furigana", []):
        body += _message(4, _ruby(segment))
    return body


def _word(kanji, examples):
    """ChunkWordExamples {kanji=1, examples=2}"""
    body = _string(1, kanji)
    for example in examples:
        body += _message(2, _example(example))
    return body


def encode_chunk(items):
    """[(kanji, [예문...])] → ExampleChunk {words=1} 직렬화 bytes."""
    return b"".join(_message(1, _word(kanji, examples)) for kanji, examples in items)


def compress(raw):
    """raw deflate (zlib 헤더 없음, RFC 1951). 앱의 inflateRaw 와 짝."""
    compressor = zlib.compressobj(level=9, wbits=-zlib.MAX_WBITS)
    return compressor.compress(raw) + compressor.flush()


def build_blobs(examples_for_level, max_bytes=MAX_BLOB_BYTES):
    """{kanji: [예문...]} → 압축 blob 목록 (문서당 1개).

    압축 후 크기가 한도를 넘으면 한자 목록을 반으로 나눠 재귀 분할한다.
    한자 정렬로 입력 순서와 무관하게 결정적인 출력을 보장한다.
    """
    items = sorted(examples_for_level.items(), key=lambda kv: kv[0])
    return _build(items, max_bytes)


def _build(items, max_bytes):
    if not items:
        return []
    blob = compress(encode_chunk(items))
    if len(blob) <= max_bytes or len(items) == 1:
        return [blob]
    mid = len(items) // 2
    return _build(items[:mid], max_bytes) + _build(items[mid:], max_bytes)


def build_readings_blob(readings, max_bytes=MAX_BLOB_BYTES):
    """{surface: reading} → ReadingsBlob 압축 blob (readings/all 단일 문서).

    surface 정렬로 결정적인 출력을 보장한다. 앱이 readings/all 문서 하나를 읽는 구조라
    분할이 불가능하므로, 사전이 커져 한도를 넘으면 즉시 실패시켜 업로드 시점에 잡는다.
    """
    body = b"".join(
        _message(1, _string(1, surface) + _string(2, reading))
        for surface, reading in sorted(readings.items())
    )
    blob = compress(body)
    if len(blob) > max_bytes:
        raise ValueError(f"readings blob {len(blob):,}B 가 한도 {max_bytes:,}B 초과 — 문서 분할 설계 필요")
    return blob
