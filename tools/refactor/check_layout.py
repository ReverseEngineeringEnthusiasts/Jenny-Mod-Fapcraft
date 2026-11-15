#!/usr/bin/env python3
"""Check the tree against the layout rules in tools/DESIGN.md.

Errors fail the run. Warnings are review prompts; pass --strict to fail on
those too. This is a structural checker, not a style checker: it never edits.
"""
import argparse
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MOD = ROOT / 'src/main/java/com/trolmastercard/sexmod'
ROOT_ALLOWED = {'Main.java', 'Reference.java', 'BuildInfo.java', 'package-info.java'}
ONE_CLASS_ALLOWED = {
    'common/block', 'common/potion', 'common/audio', 'client/particle',
    'common/entity/scene', 'common/update', 'common/guide', 'client/camera',
    'client/skin', 'network/world',
}
CLIENT_IMPORTS = (
    'com.trolmastercard.sexmod.client.',
    'net.minecraft.client.',
    'net.minecraftforge.client.',
)
MAX_PER_PACKAGE = 30
MIN_PER_PACKAGE = 2
MAX_DEPTH = 4


def java_files(path=MOD):
    return sorted(path.rglob('*.java'))


def package_of(text):
    m = re.search(r'^package\s+([\w.]+);', text, re.M)
    return m.group(1) if m else ''


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--strict', action='store_true')
    parser.add_argument('--strict-side', action='store_true')
    parser.add_argument('--max', type=int, default=MAX_PER_PACKAGE)
    args = parser.parse_args()

    errors, warnings = [], []

    # Root package: entry point, constants, build info only.
    for path in MOD.glob('*.java'):
        if path.name not in ROOT_ALLOWED:
            errors.append(f'root class {path.name}: move it into common/ or client/')

    # api/ is interfaces only.
    api = MOD / 'api'
    if api.exists():
        for path in api.rglob('*.java'):
            text = path.read_text(encoding='utf-8')
            if not re.search(r'\binterface\s+' + re.escape(path.stem) + r'\b', text):
                errors.append(f'api/{path.name}: api/ may contain interfaces only')

    # common/ importing client classes is a legacy mixed-side branch in this
    # decompiled tree. Treat it as a warning; --strict-side makes it an error.
    # New code must keep the boundary (tools/DESIGN.md R1).
    common = MOD / 'common'
    side_files = {}
    if common.exists():
        for path in common.rglob('*.java'):
            text = path.read_text(encoding='utf-8')
            hits = [line.strip() for line in text.splitlines()
                    if line.startswith('import ') and line[7:].startswith(CLIENT_IMPORTS)]
            if hits:
                side_files[path.relative_to(MOD).as_posix()] = hits
    for name, hits in sorted(side_files.items()):
        message = f'{name}: {len(hits)} client import(s) in common (legacy @SideOnly branch)'
        (errors if args.strict_side else warnings).append(message)

    # Package size and depth.
    dirs = {}
    for path in java_files():
        dirs.setdefault(path.parent, []).append(path)
    for directory, files in sorted(dirs.items()):
        rel = directory.relative_to(MOD).as_posix()
        depth = 0 if rel == '.' else len(rel.split('/'))
        if depth > MAX_DEPTH:
            errors.append(f'{rel}/: depth {depth} > {MAX_DEPTH}')
        if len(files) > args.max:
            errors.append(f'{rel}/: {len(files)} classes > {args.max}')
        has_subdirs = any(child.is_dir() for child in directory.iterdir())
        if (len(files) < MIN_PER_PACKAGE and rel not in ONE_CLASS_ALLOWED
                and rel != '.' and not has_subdirs):
            warnings.append(f'{rel}/: only {len(files)} class; merge it or justify it')

    # Numeric suffixes and renamed-bad prefixes.
    for path in java_files():
        stem = path.stem
        if re.search(r'\d+$', stem):
            errors.append(f'{path.relative_to(MOD)}: numeric class suffix')
        if re.match(r'^(Basic|Unknown)', stem) and stem != 'UnknownPacket':
            warnings.append(f'{path.relative_to(MOD)}: name describes history, not role')

    # Network packets live under network/ and implement IMessage.
    network = MOD / 'network'
    if network.exists():
        for path in network.rglob('*.java'):
            if path.name in {'PacketHandler.java', 'UnknownPacket.java', 'package-info.java'}:
                continue
            text = path.read_text(encoding='utf-8')
            if 'implements IMessage' not in text and 'extends AbstractMessage' not in text:
                warnings.append(f'{path.relative_to(MOD)}: network class does not implement IMessage')

    # Empty directories.
    for directory in sorted(MOD.rglob('*')):
        if directory.is_dir() and not any(directory.iterdir()):
            warnings.append(f'{directory.relative_to(MOD)}/: empty directory')

    for warning in warnings:
        print(f'WARN  {warning}')
    for error in errors:
        print(f'ERROR {error}')

    print(f'\n{len(errors)} errors, {len(warnings)} warnings')
    if errors or (args.strict and warnings):
        sys.exit(1)


if __name__ == '__main__':
    main()
