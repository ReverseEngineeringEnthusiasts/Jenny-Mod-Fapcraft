#!/usr/bin/env python3
"""Remove imports that are not used by code (comments and strings excluded).

A package move can add imports for names that only appeared in a Javadoc
{@link} or a comment. Those imports are noise and can even pull a client-only
type into a common package on paper. This pass removes them without touching
wildcard or static imports it cannot resolve.

Usage:
  python3 tools/refactor/prune_imports.py [--check]
"""
import argparse
import re
import sys
from pathlib import Path

from java_scan import mask_java

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java'
TEST = ROOT / 'src/test/java'


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()

    removed = 0
    for base in (SRC, TEST):
        if not base.exists():
            continue
        for path in base.rglob('*.java'):
            text = path.read_text(encoding='utf-8')
            code = mask_java(text)
            lines = text.splitlines(keepends=True)
            keep = []
            for line in lines:
                m = re.match(r'^import\s+(static\s+)?([\w.]+);', line)
                if not m:
                    keep.append(line)
                    continue
                static, fqn = m.group(1), m.group(2)
                if fqn.endswith('*'):
                    keep.append(line)
                    continue
                name = fqn.rsplit('.', 1)[1]
                # Ignore the import line itself when looking for uses.
                body = '\n'.join(l for l in code.splitlines() if not l.startswith('import '))
                if re.search(r'\b' + re.escape(name) + r'\b', body):
                    keep.append(line)
                    continue
                removed += 1
                if args.check:
                    print(f'UNUSED {path.relative_to(ROOT)}: {line.strip()}')
            new_text = ''.join(keep)
            if new_text != text and not args.check:
                path.write_text(new_text, encoding='utf-8')

    print(f'{"would remove" if args.check else "removed"} {removed} unused imports')
    if args.check and removed:
        sys.exit(1)


if __name__ == '__main__':
    main()
