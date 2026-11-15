#!/usr/bin/env python3
"""Add missing imports for referenced project types.

After a package move, a class can reference a type that stayed in another
package. This pass builds the map of top-level project types and adds the
missing single-type imports. It is idempotent and only touches imports.

Package-private types cannot be imported from another package. When one is
referenced across packages the tool prints NEEDS_PUBLIC and leaves it alone;
run tools/refactor/widen_cross_package.py first (or move the helper next to
its users).
"""
import argparse
import re
import sys
from pathlib import Path

from java_scan import mask_java, package_of

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java'
TEST = ROOT / 'src/test/java'

TYPE = re.compile(r'^(public\s+)?((?:final|abstract|strictfp)\s+)*(class|interface|enum)\s+(\w+)', re.M)


def java_files():
    return [p for base in (SRC, TEST) if base.exists() for p in base.rglob('*.java')]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()

    files = java_files()
    types = {}
    for path in files:
        text = path.read_text(encoding='utf-8')
        pkg = package_of(text)
        if not pkg:
            continue
        for m in TYPE.finditer(text):
            line_start = text.rfind('\n', 0, m.start()) + 1
            if text[line_start:m.start()].strip():
                continue  # nested type
            name = m.group(4)
            types.setdefault(name, []).append({
                'fqn': f'{pkg}.{name}',
                'public': bool(m.group(1)),
                'path': path,
            })

    ambiguous = {name: entries for name, entries in types.items() if len(entries) > 1}
    for name, entries in sorted(ambiguous.items()):
        print(f'AMBIGUOUS {name}: {[e["fqn"] for e in entries]}')

    added = 0
    needs_public = 0
    for path in files:
        text = path.read_text(encoding='utf-8')
        pkg = package_of(text)
        imports = set(re.findall(r'^import\s+([\w.]+);', text, re.M))
        wildcard = {i.rsplit('.', 1)[0] for i in imports if i.endswith('.*')}
        code = mask_java(text)
        code = re.sub(r'^import\s+.*;$', '', code, flags=re.M)
        code = re.sub(r'^package\s+.*;$', '', code, flags=re.M)

        additions = []
        for name, entries in sorted(types.items()):
            if name in ambiguous:
                continue
            entry = entries[0]
            parent = entry['fqn'].rsplit('.', 1)[0]
            if parent == pkg or parent in wildcard:
                continue
            if not entry['public']:
                if re.search(r'\b' + re.escape(name) + r'\b', code):
                    print(f'NEEDS_PUBLIC {path.relative_to(ROOT)}: {entry["fqn"]}')
                    needs_public += 1
                continue
            if not re.search(r'\b' + re.escape(name) + r'\b', code):
                continue
            if entry['fqn'] in imports:
                continue
            conflict = [i for i in imports if i.endswith('.' + name) and i != entry['fqn']]
            if conflict:
                print(f'CONFLICT {path.relative_to(ROOT)}: {name} already imported as {conflict}, need {entry["fqn"]}')
                continue
            additions.append(entry['fqn'])

        if not additions:
            continue
        m = re.search(r'^package\s+[\w.]+;\n', text, re.M)
        if not m:
            continue
        ins = ''.join(f'import {a};\n' for a in sorted(set(additions)))
        if args.check:
            print(f'WOULD_ADD {path.relative_to(ROOT)}: {len(additions)}')
        else:
            text = text[:m.end()] + '\n' + ins + text[m.end():]
            path.write_text(text, encoding='utf-8')
        added += len(additions)

    print(f'{"would add" if args.check else "added"} {added} imports; {needs_public} cross-package package-private uses')
    if needs_public:
        sys.exit(1)


if __name__ == '__main__':
    main()
