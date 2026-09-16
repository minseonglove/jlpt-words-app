# -*- coding: utf-8 -*-
"""TSV → Firestore 적재용 dict 변환 (순수 함수)."""


def _split(line):
    return line.rstrip("\n").split("\t")


def parse_words(lines):
    """nXwords.tsv 행들 → 단어 본체 dict 리스트 (입력 순서 유지 = 문서 index).

    컬럼: id, 단어, 발음, 뜻, 품사
    헤더 행(첫 컬럼이 숫자가 아님)은 건너뛴다.
    """
    result = []
    for line in lines:
        c = _split(line)
        if len(c) < 5:
            continue
        if not c[0].strip().isdigit():  # 헤더 행(번호\t단어\t...) 스킵
            continue
        result.append(
            {
                "kanji": c[1].strip(),
                "pronunciation": c[2].strip(),
                "meaning": c[3].strip().replace("\\\\n", "\n"),  # TSV의 백슬래시2+n → 실제 개행
                "partOfSpeech": c[4].strip(),
            }
        )
    return result


def parse_examples(lines):
    """all_word_meanings.tsv 행들 → {level: {kanji: [{jp,ko,tokens:[{surface,meaning}]}]}}.

    컬럼: level, source_word, source_pron, example_jp, example_ko, meaning_index, vocab_word, vocab_pron, meaning_in_context
    같은 (source_word, meaning_index) 행들을 한 예문으로 묶고, 각 행의 vocab_word/meaning_in_context를 토큰으로 모은다.
    """
    acc = {}  # level -> kanji -> meaning_index -> {jp, ko, tokens:[]}
    for line in lines:
        c = _split(line)
        if len(c) < 9:
            continue
        level, source_word, _src_pron, ex_jp, ex_ko, midx, vocab, _vpron, meaning = c[:9]
        midx = midx.strip()
        by_kanji = acc.setdefault(level, {})
        by_index = by_kanji.setdefault(source_word, {})
        ex = by_index.setdefault(midx, {"jp": ex_jp, "ko": ex_ko, "midx": midx, "tokens": []})
        ex["tokens"].append({"surface": vocab, "meaning": meaning})

    result = {}
    for level, by_kanji in acc.items():
        result[level] = {}
        for kanji, by_index in by_kanji.items():
            ordered = [by_index[i] for i in sorted(by_index, key=lambda x: int(x))]
            result[level][kanji] = ordered
    return result


def parse_readings(lines):
    """all_word_meanings.tsv 행들 → {surface: reading} 전역 dedupe (첫 등장 우선)."""
    readings = {}
    for line in lines:
        c = _split(line)
        if len(c) < 9:
            continue
        vocab, vpron = c[6], c[7]
        readings.setdefault(vocab, vpron)
    return readings


CHUNK_SIZE = 1600


def chunk_examples(examples_for_level, size=CHUNK_SIZE):
    """{kanji: [예문...]} → kanji 정렬 후 size개씩 묶은 dict 청크 리스트."""
    items = sorted(examples_for_level.items(), key=lambda kv: kv[0])
    chunks = []
    for start in range(0, len(items), size):
        chunk = dict(items[start : start + size])
        chunks.append(chunk)
    return chunks


def chunk_words(words, size=CHUNK_SIZE):
    """단어 리스트 → size개씩 {"words": [...]} 로 묶은 청크 리스트 (입력 순서 유지).

    문서 1개 = 단어 1개 구조는 다운로드 1회당 문서 읽기 과금이 단어 수만큼
    발생하므로, 배열 필드로 묶어 급수당 문서 1~2개로 줄인다.
    앱은 문서 id(청크 index)를 숫자 순으로 정렬해 이어 붙여 행 순서를 복원한다.
    """
    return [{"words": words[start : start + size]} for start in range(0, len(words), size)]


def parse_furigana(lines):
    """example_furigana.tsv 행들 → {(level, kanji, midx): [(text, ruby)]}.

    주석 형식 규칙은 scripts/furigana/furigana_core.annotated_to_segments 와 동일해야 한다
    (마커 경계 분할·제거, 루비는 직전 한자 런에 귀속). 규칙 변경 시 양쪽을 함께 수정한다.
    """
    import re

    kanji_run = re.compile(r"[㐀-鿿々〆ヶ〇]+")
    result = {}
    for line in lines:
        c = _split(line)
        if len(c) < 4:
            continue
        level, kanji, midx, annotated = c[0], c[1], c[2], c[3]
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
                ruby = annotated[i + 1 : end]
                last = None
                for last in kanji_run.finditer(plain):
                    pass
                if last is None or last.end() != len(plain):
                    raise ValueError(f"루비 앞에 한자 런이 없음: {annotated!r}")
                base = plain[last.start() :]
                plain = plain[: last.start()]
                flush()
                segments.append((base, ruby))
                i = end + 1
                continue
            plain += ch
            i += 1
        flush()
        result[(level, kanji, midx.strip())] = segments
    return result
