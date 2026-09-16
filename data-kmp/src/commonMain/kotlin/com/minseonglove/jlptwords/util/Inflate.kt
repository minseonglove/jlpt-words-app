package com.minseonglove.jlptwords.util

/**
 * raw deflate(RFC 1951, zlib 헤더 없음) 데이터를 해제한다. Firestore 예문 청크 blob 용.
 * iOS `NSData` 의 zlib 알고리즘이 raw deflate 라서 세 플랫폼 공통 형식으로 raw 를 쓴다
 * (업로드 스크립트는 zlib `wbits=-15` 로 압축한다).
 */
internal expect fun inflateRaw(data: ByteArray): ByteArray
