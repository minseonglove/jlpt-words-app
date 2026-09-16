# -*- coding: utf-8 -*-
"""적재 결과 검증 — 로컬 TSV에서 결정적으로 재생성한 콘텐츠와 원격 Firestore를 비교.

- words_n{1-5}: 청크 문서 합산 단어 수가 content_update_date/{level}.wordCount 와 일치하는지
- examples_n{1-5}, readings/all: upload.py 와 동일한 파싱 경로로 재생성한 blob 과 바이트 일치하는지
  (build_blobs / build_readings_blob 은 입력 정렬로 결정적 출력을 보장하므로 바이트 비교가 유효하다)
"""
import os
import sys

import firebase_admin
from firebase_admin import credentials, firestore

import example_blob
import parsing

LEVELS = ["n1", "n2", "n3", "n4", "n5"]
MEANINGS_TSV = "../meaning_gen/all_word_meanings.tsv"
FURIGANA_TSV = "../meaning_gen/example_furigana.tsv"


def _read_lines(path):
    with open(path, encoding="utf-8") as f:
        return f.readlines()


def _build_local_blobs():
    meanings_lines = _read_lines(MEANINGS_TSV)
    examples_all = parsing.parse_examples(meanings_lines)
    readings = parsing.parse_readings(meanings_lines)

    furigana = parsing.parse_furigana(_read_lines(FURIGANA_TSV))
    missing = 0
    for level, by_kanji in examples_all.items():
        for kanji, exs in by_kanji.items():
            for ex in exs:
                segments = furigana.get((level, kanji, ex["midx"]))
                if segments is None:
                    missing += 1
                else:
                    ex["furigana"] = segments
    if missing:
        raise SystemExit(f"furigana 누락 예문 {missing}건 — 로컬 blob 재생성 불가, generate_furigana.py 재실행 필요")

    example_blobs = {}
    for level in LEVELS:
        ex = examples_all.get(level, {})
        example_blobs[level] = example_blob.build_blobs(ex) if ex else []
    return example_blobs, example_blob.build_readings_blob(readings)


def _remote_blobs(db, name):
    """{data: bytes} 문서들을 문서 id(청크 index) 숫자 순으로 이어 붙인 bytes 리스트."""
    docs = sorted(db.collection(name).stream(), key=lambda d: int(d.id))
    return [bytes(d.to_dict()["data"]) for d in docs]


def _compare_blobs(local, remote):
    """불일치 사유 문자열, 일치하면 None."""
    if len(local) != len(remote):
        return f"blob 수 불일치 local={len(local)} remote={len(remote)}"
    mismatched = [i for i, (lo, re) in enumerate(zip(local, remote)) if lo != re]
    if mismatched:
        return f"바이트 불일치 청크 index={mismatched}"
    return None


def main():
    cred_path = os.environ["GOOGLE_APPLICATION_CREDENTIALS"]
    firebase_admin.initialize_app(credentials.Certificate(cred_path))
    db = firestore.client(database_id="words")  # 앱은 named database "words"를 사용

    example_blobs, readings_blob = _build_local_blobs()
    failures = 0

    for level in LEVELS:
        remote_count = sum(len(doc.to_dict().get("words", [])) for doc in db.collection(f"words_{level}").stream())
        meta = db.collection("content_update_date").document(level).get().to_dict() or {}
        expected = meta.get("wordCount")
        ok = remote_count == expected
        failures += 0 if ok else 1
        print(f"[{level}] words remote={remote_count} wordCount={expected} {'OK' if ok else 'MISMATCH'}")

    for level in LEVELS:
        reason = _compare_blobs(example_blobs[level], _remote_blobs(db, f"examples_{level}"))
        failures += 1 if reason else 0
        status = f"MISMATCH ({reason})" if reason else "OK"
        print(f"[{level}] example blobs={len(example_blobs[level])} {status}")

    remote_readings = (db.collection("readings").document("all").get().to_dict() or {}).get("data")
    reason = _compare_blobs([readings_blob], [bytes(remote_readings)] if remote_readings is not None else [])
    failures += 1 if reason else 0
    status = f"MISMATCH ({reason})" if reason else "OK"
    print(f"[readings] blob_bytes={len(readings_blob):,} {status}")

    if failures:
        sys.exit(f"검증 실패 {failures}건")
    print("검증 통과")


if __name__ == "__main__":
    main()
