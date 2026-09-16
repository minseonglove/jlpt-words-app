# -*- coding: utf-8 -*-
"""예문 표제어 정합성 수정분 반영: 전 레벨(n1~n5)의 examples_{level} 재적재 + examplesDate 갱신.

각 examples_{level} 컬렉션을 먼저 전부 삭제(유령 청크 방지)한 뒤 재적재한다.
words / readings / wordsDate 는 건드리지 않는다(examplesDate 만 merge 갱신).
표제어 불일치(예: 少少→少々, 綜合→総合, うそつ→うそつき)로 example_sentences FK 적재가
실패하던 문제를 바로잡기 위해, 표기를 단어장(words)에 맞춘 all_word_meanings.tsv 로 재적재한다.
"""
import argparse
import os
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import example_blob  # noqa: E402
import parsing  # noqa: E402

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
MEANINGS_TSV = os.path.join(BASE, "meaning_gen/all_word_meanings.tsv")
LEVELS = ["n1", "n2", "n3", "n4", "n5"]  # 전 레벨 깨끗이 재적재(기존 데이터 삭제 후 업로드)
BATCH_LIMIT = 450


def _commit_in_batches(db, ops):
    for start in range(0, len(ops), BATCH_LIMIT):
        batch = db.batch()
        for ref, data in ops[start : start + BATCH_LIMIT]:
            batch.set(ref, data)
        batch.commit()


def _clear_collection(db, name):
    docs = list(db.collection(name).stream())
    for start in range(0, len(docs), BATCH_LIMIT):
        batch = db.batch()
        for doc in docs[start : start + BATCH_LIMIT]:
            batch.delete(doc.reference)
        batch.commit()
    return len(docs)


def upload(dry_run=False):
    meanings_lines = open(MEANINGS_TSV, encoding="utf-8").readlines()
    examples_all = parsing.parse_examples(meanings_lines)

    example_blobs = {}
    for level in LEVELS:
        ex = examples_all.get(level, {})
        example_blobs[level] = example_blob.build_blobs(ex) if ex else []
        blob_bytes = sum(len(b) for b in example_blobs[level])
        print(f"[{level}] examples_words={len(ex)} blobs={len(example_blobs[level])} blob_bytes={blob_bytes:,}")

    if dry_run:
        print("DRY-RUN: 적재 생략")
        return

    cred_path = os.environ["GOOGLE_APPLICATION_CREDENTIALS"]
    import firebase_admin
    from firebase_admin import credentials, firestore

    firebase_admin.initialize_app(credentials.Certificate(cred_path))
    db = firestore.client(database_id="words")  # 앱은 named database "words"를 사용
    now = int(time.time() * 1000)

    for level in LEVELS:
        removed = _clear_collection(db, f"examples_{level}")
        blobs = example_blobs[level]
        ops = []
        for cidx, blob in enumerate(blobs):
            ref = db.collection(f"examples_{level}").document(str(cidx))
            ops.append((ref, {"data": blob}))
        _commit_in_batches(db, ops)
        # examplesDate 만 갱신, wordsDate 는 merge 로 보존
        db.collection("content_update_date").document(level).set(
            {"examplesDate": now}, merge=True
        )
        print(f"[{level}] examples cleared={removed} uploaded={len(blobs)} blobs, examplesDate={now}")

    print("완료")


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()
    upload(dry_run=args.dry_run)
