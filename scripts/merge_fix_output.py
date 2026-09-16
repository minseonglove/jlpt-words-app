#!/usr/bin/env python3
"""
fix_output 배치 파일들을 하나의 최종 파일로 합산
- 9컬럼 행 (에이전트가 순번 추가한 경우) 자동 수정
- 번호를 1부터 순서대로 재부여
"""

from pathlib import Path

FIX_OUTPUT_DIR = Path("/Users/gimminseong/jlpt-words/scripts/fix_output")
FINAL_DIR = Path("/Users/gimminseong/jlpt-words/scripts/final")
FINAL_DIR.mkdir(exist_ok=True)

EXPECTED_COLS = 8
HEADER = "번호\t단어\t발음\t뜻\t품사\t예문\t예문의뜻\t예문에 쓰인 단어"


def fix_row(cols):
    """9컬럼 행을 8컬럼으로 수정"""
    n = len(cols)
    if n == EXPECTED_COLS:
        return cols

    if n < EXPECTED_COLS:
        return None  # 스킵

    if n == 9:
        # 케이스 1: 에이전트가 순번을 앞에 추가한 경우
        # col[0]이 숫자이고 col[1]이 실제 번호인 경우
        try:
            seq = int(cols[0])
            actual_num = int(cols[1])
            # col[0]이 0 또는 1-based 순번인지 확인
            # col[1]이 100 이상이고 col[0]이 작은 경우 순번으로 판단
            if (seq < actual_num and seq <= 200) or (seq == 0):
                return cols[1:]  # 첫 컬럼 제거
        except (ValueError, IndexError):
            pass

        # 케이스 2: 예문의뜻 내에 여분 탭 (broken 9-column)
        # col[6]에 || 구분자가 있고 뒤에 일본어/한국어 혼합된 경우
        # col[5]=예문1, col[6]=한국어1||일본어2, col[7]=한국어2, col[8]=단어목록
        if len(cols) == 9:
            col6 = cols[6]
            if '||' in col6:
                parts = col6.split('||', 1)
                korean1 = parts[0].strip()
                japanese2 = parts[1].strip()
                # 예문 재구성: col5 || japanese2
                # 예문의뜻 재구성: korean1 || col7
                new_row = cols[:5] + [
                    cols[5] + '||' + japanese2,
                    korean1 + '||' + cols[7],
                    cols[8] if len(cols) > 8 else ''
                ]
                return new_row
            else:
                # 그냥 마지막 여분 컬럼 제거
                return cols[:EXPECTED_COLS]

    # 10컬럼 이상: 마지막 컬럼들 합치기
    return cols[:EXPECTED_COLS - 1] + ['\t'.join(cols[EXPECTED_COLS - 1:])]


def merge(level: str, total_batches: int):
    print(f"\n{'='*50}")
    print(f"{level.upper()} 합산 시작 (총 {total_batches}개 배치)")
    print(f"{'='*50}")

    final_file = FINAL_DIR / f"{level}words_with_examples_fixed.tsv"
    missing = []
    total_rows = 0
    error_rows = []
    row_num = 1

    with open(final_file, "w", encoding="utf-8") as out:
        out.write(HEADER + "\n")

        for i in range(1, total_batches + 1):
            batch_file = FIX_OUTPUT_DIR / level / f"batch_{i:03d}.tsv"

            if not batch_file.exists():
                missing.append(i)
                print(f"  ⚠️  배치 {i:03d} 누락")
                continue

            with open(batch_file, "r", encoding="utf-8") as f:
                lines = [l.rstrip('\n') for l in f if l.strip()]

            batch_rows = 0
            for line in lines:
                # 마크다운 코드블록 제거
                if line.startswith('```'):
                    continue
                cols = line.split('\t')
                ncols = len(cols)

                if ncols == 1:
                    # 단일 컬럼 (순번만 있는 행) 스킵
                    continue

                # 9컬럼 수정
                fixed = fix_row(cols)
                if fixed is None:
                    error_rows.append((i, line[:60]))
                    continue

                cols = fixed

                if len(cols) != EXPECTED_COLS:
                    error_rows.append((i, line[:60]))
                    # 그래도 포함 (최대한 보존)
                    cols = (cols + [''] * EXPECTED_COLS)[:EXPECTED_COLS]

                # 번호 재부여
                cols[0] = str(row_num)
                out.write('\t'.join(cols) + '\n')
                row_num += 1
                batch_rows += 1

            total_rows += batch_rows
            print(f"  ✓ 배치 {i:03d}: {batch_rows}행")

    print(f"\n결과:")
    print(f"  총 행: {total_rows}개")
    print(f"  누락 배치: {missing if missing else '없음'}")
    print(f"  오류 행: {len(error_rows)}개")
    if error_rows:
        for batch_num, line in error_rows[:5]:
            print(f"    배치 {batch_num}: {line}")
    print(f"  출력: {final_file}")

    return missing, error_rows


if __name__ == "__main__":
    n1_missing, n1_errors = merge("n1", 32)
    n2_missing, n2_errors = merge("n2", 25)

    print("\n" + "="*50)
    if n1_missing or n2_missing:
        print(f"⚠️  누락된 배치가 있습니다.")
    else:
        print(f"✅ 모든 배치 합산 완료!")
        total_errors = len(n1_errors) + len(n2_errors)
        if total_errors > 0:
            print(f"⚠️  처리 불가 오류 행 {total_errors}개")
        else:
            print(f"✅ 오류 없음")

    # 최종 검증
    print("\n[최종 파일 검증]")
    for level, expected_min in [("n1", 3090), ("n2", 2430)]:
        fpath = FINAL_DIR / f"{level}words_with_examples_fixed.tsv"
        lines = fpath.read_text(encoding="utf-8").splitlines()
        data = lines[1:]  # 헤더 제외
        bad = [i for i, l in enumerate(data, 1) if len(l.split('\t')) != 8]
        print(f"  {level.upper()}: {len(data)}행, 형식 오류={len(bad)}행")
        if bad:
            print(f"    오류 행 번호: {bad[:10]}")
