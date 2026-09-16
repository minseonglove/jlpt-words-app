#!/usr/bin/env python3
"""
실제 개행(\n)이 들어간 TSV 파일 복구
- [7, 2] 패턴: 예문의뜻 칼럼의 실제 개행 → 리터럴 \n으로 교체
- [9] 패턴: 여분 탭 제거
"""

from pathlib import Path

OUTPUT_DIR = Path("/Users/gimminseong/jlpt-words/scripts/output")
EXPECTED_COLS = 8


def repair_file(filepath: Path):
    lines = filepath.read_text(encoding="utf-8").splitlines()

    # 헤더 제거
    header = None
    if lines and lines[0].startswith("번호"):
        header = lines[0]
        lines = lines[1:]

    repaired = []
    i = 0
    errors = 0

    while i < len(lines):
        line = lines[i]
        if not line.strip():
            i += 1
            continue

        cols = line.split("\t")
        ncols = len(cols)

        if ncols == EXPECTED_COLS:
            repaired.append(cols)
            i += 1

        elif ncols == 7:
            # 예문의뜻이 실제 개행으로 분리된 경우
            # 다음 줄이 나머지 (예문의뜻 두번째 + 예문에 쓰인 단어)
            if i + 1 < len(lines):
                next_line = lines[i + 1]
                next_cols = next_line.split("\t")
                if len(next_cols) == 1:
                    # 예문의뜻만 남은 경우, 그 다음 줄에 예문에 쓰인 단어가 있을 수 있음
                    if i + 2 < len(lines):
                        next2 = lines[i + 2].split("\t")
                        # 예문의뜻 속 개행 복구
                        merged_col7 = cols[6] + "\\n" + next_cols[0]
                        if len(next2) == 1:
                            merged_row = cols[:6] + [merged_col7, next2[0]]
                            repaired.append(merged_row)
                            i += 3
                        else:
                            merged_row = cols[:6] + [merged_col7] + next2
                            repaired.append(merged_row)
                            i += 3
                    else:
                        merged_col7 = cols[6] + "\\n" + next_cols[0]
                        merged_row = cols[:6] + [merged_col7, ""]
                        repaired.append(merged_row)
                        i += 2
                elif len(next_cols) == 2:
                    # [7][2] 패턴: 예문의뜻\n(계속) + 예문에 쓰인 단어
                    merged_col7 = cols[6] + "\\n" + next_cols[0]
                    merged_row = cols[:6] + [merged_col7, next_cols[1]]
                    repaired.append(merged_row)
                    i += 2
                else:
                    # 알 수 없는 패턴 - 그냥 추가
                    repaired.append(cols + [""] * (EXPECTED_COLS - ncols))
                    errors += 1
                    i += 1
            else:
                repaired.append(cols + [""])
                errors += 1
                i += 1

        elif ncols > EXPECTED_COLS:
            # 여분 탭 - 마지막 칼럼들을 합침
            fixed = cols[:EXPECTED_COLS - 1] + ["\t".join(cols[EXPECTED_COLS - 1:])]
            repaired.append(fixed)
            i += 1

        else:
            # 2열 이하 - 이전 행에 붙어야 했던 것
            if repaired:
                last = repaired[-1]
                last[6] = last[6] + "\\n" + cols[0]
                if len(cols) > 1:
                    last[7] = cols[1]
            errors += 1
            i += 1

    # 검증
    bad = [i for i, r in enumerate(repaired) if len(r) != EXPECTED_COLS]

    # 출력
    result_lines = []
    for row in repaired:
        # 각 셀 내의 실제 개행을 리터럴 \n으로 교체
        fixed_row = [cell.replace("\n", "\\n") for cell in row]
        result_lines.append("\t".join(fixed_row))

    content = "\n".join(result_lines) + "\n"
    filepath.write_text(content, encoding="utf-8")

    return len(repaired), len(bad), errors


# 검증 후 문제 파일만 수정
problem_files = [
    "n2_batch_015.tsv",
]

for fname in problem_files:
    fpath = OUTPUT_DIR / fname
    if fpath.exists():
        rows, bad, err = repair_file(fpath)
        status = "✓" if bad == 0 else f"⚠️  잔여오류={bad}"
        print(f"{status} {fname}: {rows}행 복구 완료")
    else:
        print(f"✗ {fname}: 파일 없음")
