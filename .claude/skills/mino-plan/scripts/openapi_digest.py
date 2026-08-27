#!/usr/bin/env python3
"""배포된 OpenAPI 문서를 조회해 계약 작성 근거로 쓸 수 있게 간추린다.

  fetch [outfile] [--url URL]   문서를 받아 저장하고 출처·조회 시점을 출력
  index <file> [--tag TAG]      메서드·경로·태그·summary 인덱스
  show <file> <target>...       오퍼레이션 상세. target은 `경로` 또는 `경로:메서드`

종료 코드: 0 정상 · 2 사용법 오류 · 3 조회 실패
"""
import argparse
import json
import subprocess
import sys
from datetime import datetime

# 기계 판독용 OpenAPI 문서. `/api-docs`는 Swagger UI HTML 페이지라 쓰지 않는다.
DEFAULT_URL = "https://api.gguk.org/api-docs-json"
TIMEOUT_SEC = 20

EXIT_USAGE = 2
EXIT_FETCH_FAILED = 3


def die(code: int, message: str):
    print(message, file=sys.stderr)
    sys.exit(code)


def load(path: str) -> dict:
    try:
        with open(path, encoding="utf-8") as fp:
            return json.load(fp)
    except (OSError, json.JSONDecodeError) as exc:
        die(EXIT_USAGE, f"문서를 읽지 못했다: {path} — {exc}")


def operations(doc: dict):
    """(경로, 메서드, 오퍼레이션)을 문서에 적힌 순서대로 훑는다."""
    for path, methods in doc.get("paths", {}).items():
        for method, op in methods.items():
            yield path, method, op


def cmd_fetch(args) -> None:
    # 직접 HTTPS를 열지 않고 curl에 맡긴다. 사내 프록시·시스템 트러스트 스토어 설정이
    # 그대로 적용되어야 하는데, 파이썬 표준 라이브러리는 자체 인증서 묶음을 쓴다.
    try:
        done = subprocess.run(
            ["curl", "-sSfL", "--max-time", str(TIMEOUT_SEC), args.url],
            capture_output=True,
            timeout=TIMEOUT_SEC + 5,
        )
        if done.returncode != 0:
            raise RuntimeError(
                done.stderr.decode("utf-8", "replace").strip()
                or f"curl 종료 코드 {done.returncode}"
            )
        doc = json.loads(done.stdout)
    except (OSError, subprocess.SubprocessError, RuntimeError, json.JSONDecodeError) as exc:
        die(EXIT_FETCH_FAILED, f"조회 실패: {args.url} — {exc}")

    with open(args.outfile, "wb") as fp:
        fp.write(done.stdout)

    info = doc.get("info", {})
    print(f"출처: {args.url}")
    print(f"조회 시점: {datetime.now().astimezone().isoformat(timespec='seconds')}")
    print(f"문서: {info.get('title', '?')} {info.get('version', '?')}")
    print(f"오퍼레이션: {sum(1 for _ in operations(doc))}개")
    print(f"저장: {args.outfile}")


def cmd_index(args) -> None:
    doc = load(args.file)
    rows = [
        (method.upper(), path, ",".join(op.get("tags", [])), op.get("summary") or "")
        for path, method, op in operations(doc)
        if args.tag is None or args.tag in op.get("tags", [])
    ]
    if not rows:
        # 빈 결과는 사용법 오류가 아니라 조회 결과다 — "대응 API 없음" 판정의 근거가 된다.
        print(f"일치하는 오퍼레이션 없음{f' (--tag {args.tag})' if args.tag else ''}")
        return

    widths = [max(len(r[i]) for r in rows) for i in range(3)]
    for method, path, tags, summary in rows:
        print(f"{method:<{widths[0]}}  {path:<{widths[1]}}  {tags:<{widths[2]}}  {summary}")


def cmd_show(args) -> None:
    doc = load(args.file)
    for target in args.targets:
        # 경로에는 `:`가 쓰이지 않으므로 메서드 구분자로 안전하다.
        path, _, method = target.partition(":")
        methods = doc.get("paths", {}).get(path)
        if methods is None:
            die(EXIT_USAGE, f"문서에 없는 경로다: {path}")
        if method and method.lower() not in methods:
            have = ", ".join(sorted(m.upper() for m in methods))
            die(EXIT_USAGE, f"{path}에 없는 메서드다: {method.upper()} (가능: {have})")

        for name in ([method.lower()] if method else methods):
            print(f"=== {name.upper()} {path}")
            print(json.dumps(methods[name], ensure_ascii=False, indent=1))


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)

    p_fetch = sub.add_parser("fetch", help="문서를 받아 저장한다")
    p_fetch.add_argument("outfile", nargs="?", default="openapi.json")
    p_fetch.add_argument("--url", default=DEFAULT_URL)
    p_fetch.set_defaults(func=cmd_fetch)

    p_index = sub.add_parser("index", help="오퍼레이션 인덱스를 낸다")
    p_index.add_argument("file")
    p_index.add_argument("--tag")
    p_index.set_defaults(func=cmd_index)

    p_show = sub.add_parser("show", help="오퍼레이션 상세를 낸다")
    p_show.add_argument("file")
    p_show.add_argument("targets", nargs="+", metavar="경로[:메서드]")
    p_show.set_defaults(func=cmd_show)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
