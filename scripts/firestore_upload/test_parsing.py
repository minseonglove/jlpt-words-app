import parsing


def test_parse_words_maps_columns_and_index():
    rows = [
        "번호\t단어\t발음\t뜻\t품사\t예문\t예문뜻\t형태소",  # 헤더 — 스킵돼야 함
        "1\t吸う\tすう\t1.들이마시다\\\\n2.마시다\t동사\t外で…\t밖에서…\t外、吸う",
        "2\tナイフ\tナイフ\t나이프\t명사\tナイフで…\t나이프로…\tナイフ",
    ]
    words = parsing.parse_words(rows)
    assert words[0] == {
        "kanji": "吸う",
        "pronunciation": "すう",
        "meaning": "1.들이마시다\n2.마시다",
        "partOfSpeech": "동사",
    }
    assert words[1]["kanji"] == "ナイフ"
    assert words[1]["partOfSpeech"] == "명사"
    assert len(words) == 2  # 헤더 제외, 단어 2개


def test_parse_examples_groups_by_word_and_index():
    rows = [
        "n5\t吸う\tすう\t外で空気を吸う。\t밖에서 공기를 마시다.\t0\t空気\tくうき\t공기",
        "n5\t吸う\tすう\t外で空気を吸う。\t밖에서 공기를 마시다.\t0\t吸う\tすう\t들이마시다",
        "n5\t吸う\tすう\tタバコを吸う。\t담배를 피우다.\t1\tタバコ\tタバコ\t담배",
    ]
    examples = parsing.parse_examples(rows)
    sucks = examples["n5"]["吸う"]
    assert len(sucks) == 2
    assert sucks[0]["jp"] == "外で空気を吸う。"
    assert sucks[0]["ko"] == "밖에서 공기를 마시다."
    assert sucks[0]["tokens"] == [
        {"surface": "空気", "meaning": "공기"},
        {"surface": "吸う", "meaning": "들이마시다"},
    ]
    assert sucks[1]["jp"] == "タバコを吸う。"
    assert sucks[1]["tokens"] == [{"surface": "タバコ", "meaning": "담배"}]


def test_parse_readings_global_dedupe():
    rows = [
        "n5\t吸う\tすう\t…\t…\t0\t空気\tくうき\t공기",
        "n1\t別単語\t…\t…\t…\t0\t空気\tくうき\t공기(다른문맥)",
    ]
    readings = parsing.parse_readings(rows)
    assert readings["空気"] == "くうき"
    assert len(readings) == 1


def test_chunk_examples_by_1600():
    examples_for_level = {f"w{i:04d}": [{"jp": "x", "ko": "y", "tokens": []}] for i in range(5)}
    chunks = parsing.chunk_examples(examples_for_level, size=2)
    assert len(chunks) == 3
    assert list(chunks[0].keys()) == ["w0000", "w0001"]
    assert list(chunks[2].keys()) == ["w0004"]


def test_chunk_words_preserves_order():
    words = [
        {"kanji": f"단어{i}", "pronunciation": f"발음{i}", "meaning": "뜻", "partOfSpeech": "명사"}
        for i in range(5)
    ]
    chunks = parsing.chunk_words(words, size=2)
    assert len(chunks) == 3
    assert chunks[0]["words"] == words[0:2]
    assert chunks[1]["words"] == words[2:4]
    assert chunks[2]["words"] == words[4:5]
    # 청크를 이어 붙이면 원래 행 순서가 복원돼야 한다 (단어 id = 행 순서)
    assert [w for c in chunks for w in c["words"]] == words


def test_chunk_words_empty():
    assert parsing.chunk_words([]) == []


def test_parse_examples_keeps_meaning_index():
    lines = [
        "n5\t食べる\tたべる\tご飯を*食べ*ます。\t밥을 먹습니다.\t0\tご飯\tごはん\t밥\n",
        "n5\t食べる\tたべる\tパンを*食べた*。\t빵을 먹었다.\t1\tパン\tぱん\t빵\n",
    ]
    result = parsing.parse_examples(lines)
    examples = result["n5"]["食べる"]
    assert [e["midx"] for e in examples] == ["0", "1"]


def test_parse_examples_strips_midx_whitespace():
    # parse_furigana 의 키는 midx.strip() 로 정규화된다 — parse_examples 도 동일하게
    # strip 해야 두 파서의 (level, kanji, midx) 키가 대칭을 이룬다.
    lines = [
        "n5\t食べる\tたべる\tご飯を*食べ*ます。\t밥을 먹습니다.\t 0\tご飯\tごはん\t밥\n",
    ]
    result = parsing.parse_examples(lines)
    examples = result["n5"]["食べる"]
    assert examples[0]["midx"] == "0"


def test_parse_furigana():
    lines = [
        "n5\t食べる\t0\tご飯[はん]を*食[た]べ*ます。\n",
    ]
    result = parsing.parse_furigana(lines)
    assert result[("n5", "食べる", "0")] == [
        ("ご", ""), ("飯", "はん"), ("を", ""), ("食", "た"), ("べ", ""), ("ます。", ""),
    ]
