#!/usr/bin/env python3
"""Make package-private top-level types public only when they cross a package.

The design language allows package-private helpers, but only while every user
lives in the same package. After a package move that is often no longer true,
and the compiler reports "X is not public in ...; cannot be accessed from
outside package". This tool finds exactly those types and widens only them.

It deliberately does not touch members. A protected field that becomes
cross-package is a design smell and should be fixed with a method or by moving
the helper, not by widening the field.
"""
import re
import sys
from pathlib import Path

from java_scan import mask_java, package_of

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java'
TEST = ROOT / 'src/test/java'

DECL = re.compile(r'^(public\s+)?((?:final|abstract|strictfp)\s+)*(class|interface|enum)\s+(\w+)', re.M)


def java_files():
    return [p for base in (SRC, TEST) if base.exists() for p in base.rglob('*.java')]


def main():
    files = java_files()
    types = {}
    for path in files:
        text = path.read_text(encoding='utf-8')
        pkg = package_of(text)
        for m in DECL.finditer(text):
            # Only top-level declarations: the declaration is at column 0.
            line_start = text.rfind('\n', 0, m.start()) + 1
            if text[line_start:m.start()].strip():
                continue
            public = bool(m.group(1))
            name = m.group(4)
            types.setdefault(name, []).append({
                'path': path, 'pkg': pkg, 'public': public, 'name': name,
            })

    ambiguous = {name for name, entries in types.items() if len(entries) > 1}
    for name in sorted(ambiguous):
        print(f'AMBIGUOUS {name}: {[e["pkg"] for e in types[name]]}')

    widen = {}
    for path in files:
        text = path.read_text(encoding='utf-8')
        pkg = package_of(text)
        code = mask_java(text)
        # Drop imports and the package line so an import does not count as a use.
        code = re.sub(r'^import\s+.*;$', '', code, flags=re.M)
        code = re.sub(r'^package\s+.*;$', '', code, flags=re.M)
        for name, entries in types.items():
            if name in ambiguous:
                continue
            entry = entries[0]
            if entry['public'] or entry['pkg'] == pkg:
                continue
            if not re.search(r'\b' + re.escape(name) + r'\b', code):
                continue
            widen.setdefault(name, entry)

    for name, entry in sorted(widen.items()):
        path = entry['path']
        text = path.read_text(encoding='utf-8')
        pattern = re.compile(
            r'^(?!(?:public)\b)((?:final|abstract|strictfp)\s+)*(class|interface|enum)\s+'
            + re.escape(name) + r'\b', re.M)
        new_text, count = pattern.subn(
            lambda m: 'public ' + (m.group(1) or '') + m.group(2) + ' ' + name, text, count=1)
        if count:
            path.write_text(new_text, encoding='utf-8')
            print(f'public {entry["pkg"]}.{name}  ({path.relative_to(ROOT)})')
        else:
            print(f'NEEDS_MANUAL {entry["pkg"]}.{name}  ({path.relative_to(ROOT)})')

    print(f'widened {len(widen)} top-level types')


if __name__ == '__main__':
    main()
