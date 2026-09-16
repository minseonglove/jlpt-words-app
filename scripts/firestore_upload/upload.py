# -*- coding: utf-8 -*-
"""신규 Firestore 컬렉션 적재. 기존 n1~n5 / words_update_date 는 건드리지 않는다."""
import argparse
import os
import time

import firebase_admin
from firebase_admin import credentials, firestore

import example_blob
import parsing

LEVELS = ["n1", "n2", "n3", "n4", "n5"]
FINAL_DIR = "../final"
MEANINGS_TSV = "../meaning_gen/all_word_meanings.tsv"
FURIGANA_TSV = "../meaning_gen/example_furigana.tsv"
BATCH_LIMIT = 450  # Firestore batch 최대 500. 여유.


def _read_lines(path):
    with open(path, encoding="utf-8") as f:
        return f.readlines()


def _now_ms():
    return int(time.time() * 1000)


def _commit_in_batches(db, ops):
    """ops: [(doc_ref, data)] 리스트를 batch로 set."""
    for start in range(0, len(ops), BATCH_LIMIT):
        batch = db.batch()
        for ref, data in ops[start : start + BATCH_LIMIT]:
            batch.set(ref, data)
        batch.commit()


def _clear_collection(db, name):
    """컬렉션의 모든 문서를 batch로 삭제. 삭제 건수 반환.

    단어 삭제로 문서 수가 줄어든 경우, set만으로는 옛 index 문서가 유령으로 남는다.
    재적재 전 비워서 이전 상태와 무관하게 깨끗한 상태를 보장한다.
    """
    docs = list(db.collection(name).stream())
    for start in range(0, len(docs), BATCH_LIMIT):
        batch = db.batch()
        for doc in docs[start : start + BATCH_LIMIT]:
            batch.delete(doc.reference)
        batch.commit()
    return len(docs)


def upload(dry_run=False):
    meanings_lines = _read_lines(MEANINGS_TSV)
    examples_all = parsing.parse_examples(meanings_lines)
    readings = parsing.parse_readings(meanings_lines)

    furigana = parsing.parse_furigana(_read_lines(FURIGANA_TSV))
    furigana_missing = 0
    for level, by_kanji in examples_all.items():
        for kanji, exs in by_kanji.items():
            for ex in exs:
                segments = furigana.get((level, kanji, ex["midx"]))
                if segments is None:
                    furigana_missing += 1
                else:
                    ex["furigana"] = segments
    print(f"[furigana] entries={len(furigana)} missing={furigana_missing}")
    if furigana_missing:
        raise SystemExit(f"furigana 누락 예문 {furigana_missing}건 — generate_furigana.py 재실행 필요")

    words_by_level = {}
    for level in LEVELS:
        path = f"{FINAL_DIR}/{level}words.tsv"
        words_by_level[level] = parsing.parse_words(_read_lines(path))

    # 예문은 protobuf(ExampleChunk) 직렬화 + raw deflate 압축 blob 으로 적재한다.
    example_blobs = {}
    for level in LEVELS:
        ex = examples_all.get(level, {})
        example_blobs[level] = example_blob.build_blobs(ex) if ex else []

    for level in LEVELS:
        ex = examples_all.get(level, {})
        blobs = example_blobs[level]
        blob_bytes = sum(len(b) for b in blobs)
        n_word_chunks = len(parsing.chunk_words(words_by_level[level]))
        print(
            f"[{level}] words={len(words_by_level[level])} word_chunks={n_word_chunks}"
            f" examples_words={len(ex)} example_blobs={len(blobs)} blob_bytes={blob_bytes:,}"
        )
    print(f"[readings] surfaces={len(readings)} blob_bytes={len(example_blob.build_readings_blob(readings)):,}")

    if dry_run:
        print("DRY-RUN: 적재 생략")
        return

    cred_path = os.environ["GOOGLE_APPLICATION_CREDENTIALS"]
    firebase_admin.initialize_app(credentials.Certificate(cred_path))
    db = firestore.client(database_id="words")  # 앱은 named database "words"를 사용
    now = _now_ms()

    for level in LEVELS:
        removed = _clear_collection(db, f"words_{level}")
        chunks = parsing.chunk_words(words_by_level[level])
        ops = []
        for cidx, chunk in enumerate(chunks):
            ref = db.collection(f"words_{level}").document(str(cidx))
            ops.append((ref, chunk))
        _commit_in_batches(db, ops)
        print(f"[{level}] word chunks cleared={removed} uploaded={len(chunks)} words={len(words_by_level[level])}")

    for level in LEVELS:
        removed = _clear_collection(db, f"examples_{level}")
        blobs = example_blobs[level]
        ops = []
        for cidx, blob in enumerate(blobs):
            ref = db.collection(f"examples_{level}").document(str(cidx))
            ops.append((ref, {"data": blob}))
        _commit_in_batches(db, ops)
        print(f"[{level}] example blobs cleared={removed} uploaded={len(blobs)}")

    readings_blob = example_blob.build_readings_blob(readings)
    db.collection("readings").document("all").set({"data": readings_blob})
    print(f"[readings] uploaded: {len(readings)} entries, {len(readings_blob):,}B blob")

    # wordCount: 단어가 청크 문서로 묶이면서 문서 수 ≠ 단어 수가 되어,
    # 앱의 급수별 단어 수 조회(레벨 선택 화면)는 이 메타데이터를 읽는다.
    for level in LEVELS:
        db.collection("content_update_date").document(level).set(
            {"wordsDate": now, "examplesDate": now, "wordCount": len(words_by_level[level])}
        )
    db.collection("content_update_date").document("readings").set({"date": now})
    print(f"[dates] set to {now}")


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()
    upload(dry_run=args.dry_run)
