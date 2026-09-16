# -*- coding: utf-8 -*-
"""kanji_readings.tsv → Firestore kanji_info/all 단일 문서 적재.

전체 데이터가 ~190KB 라 Firestore 1 MiB 문서 제한 내. readings/all 과 동일하게
단일 문서(map)로 저장해 읽기 1회로 동기화한다.

문서: kanji_info/all
  { "<한자>": { korean_hanja?: str, on_yomi?: [str], kun_yomi?: [str] }, ... }
  (각 필드는 값이 없으면 생략)

날짜: content_update_date/kanji  { date: <timestamp_ms> }
"""
import argparse
import os
import time

import firebase_admin
from firebase_admin import credentials, firestore

TSV_PATH = os.path.join(os.path.dirname(__file__), "../../docs/kanji_readings.tsv")


def _parse_tsv(path):
    """TSV → {한자: {korean_hanja?, on_yomi?, kun_yomi?}} (빈 값은 필드 생략)."""
    result = {}
    with open(path, encoding="utf-8") as f:
        lines = f.readlines()

    for line in lines[1:]:  # 헤더 스킵
        parts = line.rstrip("\n").split("\t")
        if len(parts) < 4:
            continue
        kanji, korean_hanja, on_yomi_raw, kun_yomi_raw = parts[:4]
        if not kanji:
            continue

        entry = {}
        if korean_hanja.strip():
            entry["korean_hanja"] = korean_hanja.strip()
        if on_yomi_raw.strip():
            entry["on_yomi"] = [r.strip() for r in on_yomi_raw.split("·") if r.strip()]
        if kun_yomi_raw.strip():
            entry["kun_yomi"] = [r.strip() for r in kun_yomi_raw.split("·") if r.strip()]

        result[kanji] = entry
    return result


def upload(dry_run=False):
    kanji_map = _parse_tsv(TSV_PATH)
    print(f"TSV 파싱 완료: {len(kanji_map)}개 한자")

    if dry_run:
        print("DRY-RUN: 적재 생략 — 샘플 3개")
        for k in list(kanji_map)[:3]:
            print(f"  {k}: {kanji_map[k]}")
        return

    cred_path = os.environ["GOOGLE_APPLICATION_CREDENTIALS"]
    firebase_admin.initialize_app(credentials.Certificate(cred_path))
    db = firestore.client(database_id="words")

    # kanji_info/all 단일 문서로 set (기존 개별 문서가 있다면 정리)
    _delete_legacy_per_kanji_docs(db)
    db.collection("kanji_info").document("all").set(kanji_map)
    print(f"kanji_info/all 업로드 완료: {len(kanji_map)}개 한자 (단일 문서)")

    now_ms = int(time.time() * 1000)
    db.collection("content_update_date").document("kanji").set({"date": now_ms})
    print(f"content_update_date/kanji 설정: {now_ms}")


def _delete_legacy_per_kanji_docs(db):
    """이전 버전이 만든 kanji_info/{한자} 개별 문서를 일괄 삭제 (all 문서는 보존)."""
    collection = db.collection("kanji_info")
    deleted = 0
    batch = db.batch()
    batch_count = 0
    for doc in collection.stream():
        if doc.id == "all":
            continue
        batch.delete(doc.reference)
        batch_count += 1
        deleted += 1
        if batch_count >= 450:
            batch.commit()
            batch = db.batch()
            batch_count = 0
    if batch_count > 0:
        batch.commit()
    if deleted:
        print(f"기존 개별 문서 삭제: {deleted}개")


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()
    upload(dry_run=args.dry_run)
