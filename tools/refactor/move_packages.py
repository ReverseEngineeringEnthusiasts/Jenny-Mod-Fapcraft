#!/usr/bin/env python3
"""Move top-level Java classes to new packages and repair imports."""
import argparse
import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java'
TEST = ROOT / 'src/test/java'
parser = argparse.ArgumentParser()
parser.add_argument('mapping')
parser.add_argument('--dry-run', action='store_true')
args = parser.parse_args()
mapping = json.loads(Path(args.mapping).read_text())


def java_files():
    return [p for base in (SRC, TEST) if base.exists() for p in base.rglob('*.java')]


def file_pkg(path):
    text = path.read_text(encoding='utf-8')
    m = re.search(r'^package\s+([\w.]+);', text, re.M)
    return m.group(1) if m else ''


def main():
    files = java_files()
    old_path = {}
    for p in files:
        pkg = file_pkg(p)
        if pkg:
            old_path[f'{pkg}.{p.stem}'] = p
    missing = [k for k in mapping if k not in old_path]
    if missing:
        sys.exit('missing classes: ' + ', '.join(missing))

    new_fqn = {}
    for old, new_pkg in mapping.items():
        simple = old.rsplit('.', 1)[1]
        new_fqn[old] = f'{new_pkg}.{simple}'

    # git mv
    moved_paths = {}
    for old, new_pkg in mapping.items():
        src = old_path[old]
        base = SRC if SRC in src.parents else TEST
        dst = base / new_pkg.replace('.', '/') / (src.stem + '.java')
        if src.resolve() == dst.resolve():
            moved_paths[old] = src
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        if args.dry_run:
            print('mv', src.relative_to(ROOT), '->', dst.relative_to(ROOT))
        else:
            subprocess.run(['git', 'mv', str(src), str(dst)], check=True)
        moved_paths[old] = dst

    if args.dry_run:
        print(f'would move {len(mapping)} classes')
        return

    # fully-qualified replacements (imports, static imports, javadoc).
    # Word boundaries keep Foo from matching FooBar.
    for p in java_files():
        text = p.read_text(encoding='utf-8')
        new_text = text
        for old, new in new_fqn.items():
            new_text = re.sub(r'\b' + re.escape(old) + r'\b', new.replace('\\', '\\\\'), new_text)
        if new_text != text:
            p.write_text(new_text, encoding='utf-8')

    # package declarations in moved files
    for old, new_pkg in mapping.items():
        p = moved_paths[old]
        text = p.read_text(encoding='utf-8')
        text = re.sub(r'^package\s+[\w.]+;', f'package {new_pkg};', text, count=1, flags=re.M)
        p.write_text(text, encoding='utf-8')

    # simple-name map (top-level moved types)
    simple = {}
    for old, new in new_fqn.items():
        simple.setdefault(old.rsplit('.', 1)[1], []).append((old, new))
    dupes = {k: v for k, v in simple.items() if len(v) > 1}
    if dupes:
        sys.exit('duplicate simple names: ' + repr(dupes))

    # add imports
    added = 0
    for p in java_files():
        text = p.read_text(encoding='utf-8')
        pkg = file_pkg(p)
        imports = set(re.findall(r'^import\s+([\w.]+);', text, re.M))
        additions = []
        for name, entries in simple.items():
            old, new = entries[0]
            if not re.search(r'\b' + re.escape(name) + r'\b', text):
                continue
            if new.rsplit('.', 1)[0] == pkg:
                continue
            if new in imports:
                continue
            conflict = [i for i in imports if i.endswith('.' + name) and i != new]
            if conflict:
                print(f'CONFLICT {p}: {name} already imported as {conflict}, need {new}')
                continue
            additions.append(new)
        if additions:
            # insert after the package line
            m = re.search(r'^package\s+[\w.]+;\n', text, re.M)
            if not m:
                sys.exit('no package line in ' + str(p))
            ins = ''.join(f'import {a};\n' for a in additions)
            text = text[:m.end()] + '\n' + ins + text[m.end():]
            p.write_text(text, encoding='utf-8')
            added += len(additions)
    # Remove directories emptied by the move.
    for base in (SRC, TEST):
        for directory in sorted((d for d in base.rglob('*') if d.is_dir()),
                                key=lambda d: len(d.parts), reverse=True):
            try:
                directory.rmdir()
            except OSError:
                pass

    print(f'moved {len(mapping)} classes, added {added} imports')


if __name__ == '__main__':
    main()
