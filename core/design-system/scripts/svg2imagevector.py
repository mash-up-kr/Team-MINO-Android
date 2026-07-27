#!/usr/bin/env python3
"""Figma에서 export한 단색 아이콘 SVG를 Compose ImageVector Kotlin 파일로 변환한다.

사용법: python3 svg2imagevector.py <input.svg> <iconNameCamelCase> <output_dir>
"""
import re
import sys
import xml.etree.ElementTree as ET

SVG_NS = "{http://www.w3.org/2000/svg}"

CMD_ARITY = {
    "M": 2, "L": 2, "H": 1, "V": 1, "C": 6, "S": 4, "Q": 4, "T": 2, "A": 7, "Z": 0,
}

# A(arcTo)와 Z(close)는 인자 형태가 달라 별도 처리한다.
CMD_METHODS = {
    "M": "moveTo", "L": "lineTo", "H": "horizontalLineTo", "V": "verticalLineTo",
    "C": "curveTo", "S": "reflectiveCurveTo", "Q": "quadTo", "T": "reflectiveQuadTo",
}

NUM_RE = re.compile(r"[-+]?(?:\d*\.\d+|\d+\.?)(?:[eE][-+]?\d+)?")


def num(n: float) -> str:
    s = f"{n:.4f}".rstrip("0").rstrip(".")
    return "0" if s in ("-0", "") else s


def fmt(n: float) -> str:
    return num(n) + "f"


def tokenize_path(d: str):
    """(command, [numbers...]) 시퀀스로 분해. 반복 인자는 커맨드를 복제한다."""
    out = []
    for m in re.finditer(r"([MmLlHhVvCcSsQqTtAaZz])([^MmLlHhVvCcSsQqTtAaZz]*)", d):
        cmd, argstr = m.group(1), m.group(2)
        nums = [float(x) for x in NUM_RE.findall(argstr)]
        arity = CMD_ARITY[cmd.upper()]
        if arity == 0:
            out.append((cmd, []))
            continue
        if len(nums) % arity != 0:
            raise ValueError(f"path 인자 개수 불일치: {cmd} {nums}")
        for i in range(0, len(nums), arity):
            # M의 두 번째 이후 좌표쌍은 암시적 L
            eff = {"M": "L", "m": "l"}.get(cmd, cmd) if i > 0 else cmd
            out.append((eff, nums[i:i + arity]))
    return out


def emit_call(cmd: str, a: list[float]) -> str:
    rel = "Relative" if cmd.islower() else ""
    u = cmd.upper()
    if u == "Z":
        return "close()"
    if u == "A":
        large = "true" if a[3] else "false"
        sweep = "true" if a[4] else "false"
        args = [fmt(a[0]), fmt(a[1]), fmt(a[2]), large, sweep, fmt(a[5]), fmt(a[6])]
        return f"arcTo{rel}({', '.join(args)})"
    if u not in CMD_METHODS:
        raise ValueError(f"미지원 커맨드: {cmd}")
    return f"{CMD_METHODS[u]}{rel}({', '.join(fmt(x) for x in a)})"


def parse_fill(attr_fill: str | None) -> str | None:
    """fill 속성 → Kotlin 색 표현. fill 없음/none이면 None.

    fill은 틴트로 덮어쓰는 자리값이므로 토큰과 연동하지 않고 리터럴로 굽는다.
    """
    if attr_fill is None or attr_fill == "none":
        return None
    m = re.search(r"#[0-9a-fA-F]{6}", attr_fill)
    if not m:
        raise ValueError(f"해석 불가한 fill: {attr_fill}")
    return f"Color(0xFF{m.group(0)[1:].upper()})"


def collect_paths(elem, inherited_fill=None):
    """<g> 상속 fill을 반영하며 (d, fill, fillRule, opacity) 수집. transform은 미지원으로 검출."""
    paths = []
    tag = elem.tag.removeprefix(SVG_NS)
    if elem.get("transform"):
        raise ValueError(f"transform 있는 <{tag}> 발견 — 수동 확인 필요")
    fill = elem.get("fill", inherited_fill)
    if tag == "path":
        if elem.get("stroke") not in (None, "none"):
            raise ValueError("stroke 사용 path 발견 — 수동 확인 필요")
        rule = elem.get("fill-rule") or elem.get("clip-rule")
        opacity = float(elem.get("fill-opacity", elem.get("opacity", "1")))
        paths.append((elem.get("d"), fill, rule, opacity))
    for child in elem:
        paths.extend(collect_paths(child, fill))
    return paths


def convert(svg_file: str, name: str, out_dir: str):
    tree = ET.parse(svg_file)
    root = tree.getroot()
    vb = [float(x) for x in root.get("viewBox").split()]
    if vb[0] != 0.0 or vb[1] != 0.0:
        raise ValueError(f"viewBox 원점이 0,0이 아님: {vb}")
    vp_w, vp_h = vb[2], vb[3]

    pascal = name[0].upper() + name[1:]

    path_blocks = []
    for d, fill, rule, opacity in collect_paths(root):
        color_expr = parse_fill(fill)
        if color_expr is None:
            continue
        args = [f"fill = SolidColor({color_expr})"]
        if opacity != 1.0:
            args.append(f"fillAlpha = {fmt(opacity)}")
        if rule == "evenodd":
            args.append("pathFillType = PathFillType.EvenOdd")
        block = [f"            path({', '.join(args)}) {{"]
        block += [f"                {emit_call(cmd, a)}" for cmd, a in tokenize_path(d)]
        block.append("            }")
        path_blocks.append("\n".join(block))

    if path_blocks:
        body = ".apply {\n" + "\n".join(path_blocks) + "\n        }.build()"
    else:
        # 빈 아이콘(blank): path 없이 빈 벡터
        body = ".build()"

    imports = [
        "androidx.compose.foundation.background",
        "androidx.compose.foundation.layout.padding",
        "androidx.compose.foundation.layout.size",
        "androidx.compose.material3.Icon",
        "androidx.compose.runtime.Composable",
        "androidx.compose.ui.Modifier",
        "androidx.compose.ui.graphics.vector.ImageVector",
        "androidx.compose.ui.unit.dp",
        "team.mino.core.designsystem.foundation.color.token.ColorAccessKeyToken",
        "team.mino.core.designsystem.foundation.color.token.value",
        "team.mino.core.designsystem.foundation.icons.MinoIcons",
        "team.mino.core.designsystem.theme.MinoAndroidAppTheme",
        "team.mino.core.designsystem.util.preview.UiModePreviews",
    ]
    if "SolidColor(" in body:
        imports.append("androidx.compose.ui.graphics.SolidColor")
        imports.append("androidx.compose.ui.graphics.vector.path")
    if "PathFillType." in body:
        imports.append("androidx.compose.ui.graphics.PathFillType")
    if "Color(0x" in body:
        imports.append("androidx.compose.ui.graphics.Color")
    import_lines = "\n".join(f"import {i}" for i in sorted(set(imports)))

    kotlin = f"""package team.mino.core.designsystem.foundation.icons.icons

{import_lines}

val MinoIcons.{pascal}: ImageVector by lazy {{
    ImageVector
        .Builder(
            name = "MinoIcons.{pascal}",
            defaultWidth = {num(vp_w)}.dp,
            defaultHeight = {num(vp_h)}.dp,
            viewportWidth = {fmt(vp_w)},
            viewportHeight = {fmt(vp_h)},
        ){body}
}}

@UiModePreviews
@Composable
private fun {pascal}Preview() {{
    MinoAndroidAppTheme {{
        Icon(
            imageVector = MinoIcons.{pascal},
            contentDescription = null,
            modifier = Modifier
                .background(ColorAccessKeyToken.BackgroundNormalNormal.value)
                .padding(8.dp)
                .size(24.dp),
            tint = ColorAccessKeyToken.LabelNormal.value,
        )
    }}
}}
"""
    out_path = f"{out_dir}/{pascal}.kt"
    with open(out_path, "w") as f:
        f.write(kotlin)
    print(f"  생성: {out_path}")


if __name__ == "__main__":
    convert(sys.argv[1], sys.argv[2], sys.argv[3])
