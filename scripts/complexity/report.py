#!/usr/bin/env python3
"""detekt 복잡도 리포트 집계.

각 모듈의 build/reports/detekt/detekt.xml 을 읽어 전 함수의 순환복잡도(CC)와
인지 복잡도를 하나로 합치고, 분포·모듈별 요약·상위 랭킹을 출력한다.
--snapshot 을 주면 docs/complexity/ 아래에 CSV 를 남겨 추이를 추적할 수 있다.

  python3 scripts/complexity/report.py
  python3 scripts/complexity/report.py --top 30 --snapshot --label before
"""

import argparse
import csv
import datetime
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

# detekt.yml 에 룰을 새로 켜면 아래 셋 중 하나에 등록한다 (빠뜨리면 실행 시 경고가 뜬다).
# (1) 함수 단위로 수치를 주는 룰 — (함수명, 값) 을 뽑아 해당 필드에 담는다
RULE_PATTERNS = {
    "CyclomaticComplexMethod": re.compile(r"The function (\S+) .*?\(complexity: (\d+)\)"),
    "CognitiveComplexMethod": re.compile(r"The function (\S+) .*?\(complexity: (\d+)\)"),
    "LongMethod": re.compile(r"The function (\S+) is too long \((\d+)\)"),
}
FIELD_OF = {"CyclomaticComplexMethod": "cc", "CognitiveComplexMethod": "cognitive",
            "LongMethod": "length"}

# (2) 함수 단위지만 수치 없이 위반 여부만 주는 룰 — 플래그로 표시한다
FLAG_PATTERNS = {"NestedBlockDepth": re.compile(r"Function (\S+) is nested too deeply")}

# (3) 함수에 귀속되지 않는 룰(조건식 단위 등) — 건수만 따로 보고한다
UNMAPPED_RULES = {"ComplexCondition"}

# CC 계급 구간 — 상한 미만
BANDS = [(1, 5, "1-4    단순"), (5, 11, "5-10   보통"), (11, 16, "11-15  주의"),
         (16, 21, "16-20  경고"), (21, 10**9, "21+    위험")]

_LINE_CACHE = {}


def modules():
    """settings.gradle.kts 의 include 목록을 그대로 따른다."""
    text = (ROOT / "settings.gradle.kts").read_text(encoding="utf-8")
    return [m.lstrip(":").replace(":", "/") for m in re.findall(r'include\("([^"]+)"\)', text)]


def source_lines(path: Path):
    if path not in _LINE_CACHE:
        try:
            _LINE_CACHE[path] = path.read_text(encoding="utf-8").splitlines()
        except OSError:
            _LINE_CACHE[path] = []
    return _LINE_CACHE[path]


def band_of(cc):
    for lo, hi, label in BANDS:
        if lo <= cc < hi:
            return label
    return BANDS[0][2]


def is_composable(path: Path, line: int) -> bool:
    """함수 선언 위쪽의 어노테이션 블록에 @Composable 이 있는지 본다."""
    lines = source_lines(path)
    i = line - 2  # 0-index 로 바꾼 뒤 선언 바로 윗줄부터
    while i >= 0:
        stripped = lines[i].strip()
        if stripped.startswith("@"):
            if "Composable" in stripped:
                return True
            i -= 1
            continue
        if stripped == "" or stripped.endswith(")") or stripped.endswith(","):
            # 여러 줄 어노테이션 인자 도중일 수 있어 한 줄 더 본다
            i -= 1
            if i >= 0 and lines[i].strip().startswith("@"):
                continue
        return False
    return False


def churn(since):
    """파일별 커밋 횟수. 리팩토링 대상을 고를 때 쓰는 '변경 빈도' 축."""
    cmd = ["git", "-C", str(ROOT), "log", "--format=format:", "--name-only"]
    if since:
        cmd += ["--since", since]
    try:
        out = subprocess.run(cmd, capture_output=True, text=True, check=True).stdout
    except (OSError, subprocess.CalledProcessError):
        return {}
    counts = defaultdict(int)
    for line in out.splitlines():
        line = line.strip()
        if line.endswith(".kt"):
            counts[line] += 1
    return counts


def collect():
    """함수 단위 레코드로 합친다. 키는 (파일, 라인, 함수명)."""
    records, missing, unparsed, unmapped = {}, [], defaultdict(int), defaultdict(int)
    for module in modules():
        report = ROOT / module / "build" / "reports" / "detekt" / "detekt.xml"
        if not report.exists():
            missing.append(module)
            continue
        for file_node in ET.parse(report).getroot().iter("file"):
            file_path = Path(file_node.get("name"))
            rel = file_path.relative_to(ROOT) if file_path.is_absolute() else file_path
            for err in file_node.iter("error"):
                rule = (err.get("source") or "").removeprefix("detekt.")
                message = err.get("message") or ""
                if rule in UNMAPPED_RULES:
                    unmapped[rule] += 1
                    continue
                pattern = RULE_PATTERNS.get(rule) or FLAG_PATTERNS.get(rule)
                match = pattern.search(message) if pattern else None
                if not match:
                    unparsed[rule] += 1
                    continue
                line = int(err.get("line"))
                key = (str(rel), line, match.group(1))
                rec = records.setdefault(key, {
                    "module": module, "file": str(rel), "line": line,
                    "name": match.group(1), "cc": 0, "cognitive": 0, "length": 0,
                    "nested": False, "composable": is_composable(ROOT / rel, line),
                })
                if rule in FLAG_PATTERNS:
                    rec["nested"] = True
                else:
                    rec[FIELD_OF[rule]] = int(match.group(2))
    return [r for r in records.values() if r["cc"] > 0], missing, unparsed, unmapped


def bar(count, total, width=40):
    filled = 0 if not total else round(count / total * width)
    return "█" * filled + "·" * (width - filled)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--top", type=int, default=20, help="상위 랭킹 개수")
    ap.add_argument("--sort", choices=("cc", "cognitive"), default="cc",
                    help="랭킹 정렬 기준 (기본: cc)")
    ap.add_argument("--snapshot", action="store_true", help="docs/complexity/ 에 CSV 저장")
    ap.add_argument("--label", help="스냅샷 파일명에 붙일 꼬리표 (예: before, after)")
    ap.add_argument("--exclude-composable", action="store_true",
                    help="@Composable 함수를 집계 전체에서 제외")
    ap.add_argument("--hotspot", action="store_true",
                    help="복잡도 x 변경빈도(git) 로 리팩토링 우선순위를 낸다")
    ap.add_argument("--since", default="1 year ago",
                    help="--hotspot 의 집계 기간 (기본: 1 year ago, 전체는 빈 문자열)")
    args = ap.parse_args()

    records, missing, unparsed, unmapped = collect()
    if missing:
        print(f"! 리포트 없음: {', '.join(missing)}  (./gradlew detekt 먼저 실행)\n",
              file=sys.stderr)
    if unparsed:
        detail = ", ".join(f"{rule} {n}건" for rule, n in sorted(unparsed.items()))
        print(f"! 집계하지 못한 룰: {detail}  (RULE_PATTERNS 에 추가 필요)\n", file=sys.stderr)
    if unmapped:
        detail = ", ".join(f"{rule} {n}건" for rule, n in sorted(unmapped.items()))
        print(f"  (함수에 귀속되지 않아 랭킹에서 빠진 위반: {detail} — HTML 리포트 참고)")
    if not records:
        print("계측된 함수가 없습니다. ./gradlew detekt 를 실행하세요.", file=sys.stderr)
        return 1

    composables = [r for r in records if r["composable"]]
    if args.exclude_composable:
        records = [r for r in records if not r["composable"]]
        if not records:
            print("@Composable 을 빼고 나면 남는 함수가 없습니다.", file=sys.stderr)
            return 1

    total = len(records)
    ccs = sorted(r["cc"] for r in records)
    mean = sum(ccs) / total
    p50, p90, p99 = (ccs[min(int(total * q), total - 1)] for q in (0.50, 0.90, 0.99))

    scope = " (@Composable 제외)" if args.exclude_composable else ""
    print(f"\n총 함수 {total}개{scope}   평균 CC {mean:.2f}   중앙값 {p50}   "
          f"p90 {p90}   p99 {p99}   최대 {ccs[-1]}")

    print("\n■ 복잡도 분포")
    counts = defaultdict(int)
    for r in records:
        counts[band_of(r["cc"])] += 1
    for _, _, label in BANDS:
        n = counts[label]
        print(f"  {label}  {n:4d}  {bar(n, total)} {n / total * 100:5.1f}%")

    print("\n■ 모듈별")
    by_module = defaultdict(list)
    for r in records:
        by_module[r["module"]].append(r["cc"])
    print(f"  {'모듈':<18}{'함수':>6}{'평균':>8}{'최대':>6}{'CC>10':>8}")
    for module in modules():
        vals = by_module.get(module)
        if not vals:
            continue
        over = sum(1 for v in vals if v > 10)
        print(f"  {module:<18}{len(vals):>6}{sum(vals) / len(vals):>8.2f}{max(vals):>6}{over:>8}")

    # CC 는 when 분기 수에, 인지 복잡도는 중첩 깊이에 반응한다. 한쪽만 보면
    # 디스패처(CC 높음)나 중첩 람다(인지 높음) 중 하나를 놓친다.
    ranked = (sorted(records, key=lambda r: (-r["cc"], -r["cognitive"])) if args.sort == "cc"
              else sorted(records, key=lambda r: (-r["cognitive"], -r["cc"])))
    label = "CC" if args.sort == "cc" else "인지 복잡도"
    print(f"\n■ 상위 {args.top}개 ({label} 내림차순)")
    print(f"  {'CC':>3}{'인지':>5}{'줄':>5}  {'함수':<34}{'위치'}")
    for r in ranked[:args.top]:
        tag = (" @C" if r["composable"] else "") + ("*" if r["nested"] else "")
        name = (r["name"] + tag)[:33]
        length = r["length"] or "-"
        print(f"  {r['cc']:>3}{r['cognitive']:>5}{length:>5}  {name:<34}{r['file']}:{r['line']}")
    print("  ('줄' 은 LongMethod 한도 초과분만, '*' 은 중첩 깊이 한도 초과)")

    if args.hotspot:
        counts = churn(args.since)
        if not counts:
            print("\n! git 이력을 읽지 못해 hotspot 을 건너뜁니다.", file=sys.stderr)
        else:
            for r in records:
                r["commits"] = counts.get(r["file"], 0)
                r["hotspot"] = r["cc"] * r["commits"]
            hot = sorted((r for r in records if r["hotspot"] > 0), key=lambda r: -r["hotspot"])
            period = args.since or "전체 기간"
            print(f"\n■ 리팩토링 우선순위: CC x 파일 변경횟수 ({period})")
            print(f"  {'점수':>5}{'CC':>4}{'커밋':>5}  {'함수':<34}{'위치'}")
            for r in hot[:args.top]:
                tag = " @C" if r["composable"] else ""
                name = (r["name"] + tag)[:33]
                print(f"  {r['hotspot']:>5}{r['cc']:>4}{r['commits']:>5}  "
                      f"{name:<34}{r['file']}:{r['line']}")

    if composables and not args.exclude_composable:
        logic = [r for r in records if not r["composable"]]
        print(f"\n  참고: @Composable {len(composables)}개 "
              f"(평균 CC {sum(r['cc'] for r in composables) / len(composables):.2f}) / "
              f"일반 {len(logic)}개 (평균 CC {sum(r['cc'] for r in logic) / len(logic):.2f})")

    if args.snapshot:
        out_dir = ROOT / "docs" / "complexity"
        out_dir.mkdir(parents=True, exist_ok=True)
        stem = datetime.date.today().isoformat() + (f"-{args.label}" if args.label else "")
        out = out_dir / f"{stem}.csv"
        if out.exists():
            # 같은 날 두 번째 실행이 첫 스냅샷을 지우지 않도록 시각을 덧붙인다
            out = out_dir / f"{stem}-{datetime.datetime.now():%H%M%S}.csv"
        with out.open("w", newline="", encoding="utf-8") as fh:
            writer = csv.DictWriter(
                fh,
                fieldnames=["module", "file", "line", "name", "cc", "cognitive", "length",
                            "nested", "composable", "commits", "hotspot"],
                extrasaction="ignore", lineterminator="\n")
            writer.writeheader()
            writer.writerows(sorted(records, key=lambda r: (-r["cc"], r["file"])))
        print(f"\n스냅샷 저장: {out.relative_to(ROOT)}")
    print()
    return 0


if __name__ == "__main__":
    sys.exit(main())
