#!/usr/bin/env python3
"""Update a Prism/MMC Cleanroom instance patch to a Cleanroom release.

This targets the MMC instance layout produced by CleanroomMMC: a
patches/net.minecraftforge.json component and mmc-pack.json. It updates the
Cleanroom universal, cleanmix and mixinextras-common entries (the libraries
that change between 0.7.x releases) and the version fields, leaving the
vanilla/placeholder libraries and the org.lwjgl component alone.

Usage:
  scripts/update-cleanroom.py --instance "<instance dir>" --version 0.7.14-alpha
"""
import argparse
import json
import shutil
import sys
import time
import urllib.request
import zipfile
from pathlib import Path

RELEASES = 'https://github.com/CleanroomMC/Cleanroom/releases/download'
MAVEN = 'https://maven.cleanroommc.com'


def download(url: str, dest: Path) -> None:
    if dest.exists():
        return
    dest.parent.mkdir(parents=True, exist_ok=True)
    print(f'downloading {url}')
    with urllib.request.urlopen(url) as response, open(dest, 'wb') as out:
        shutil.copyfileobj(response, out)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument('--instance', required=True)
    parser.add_argument('--version', required=True)
    parser.add_argument('--cache', default=str(Path.home() / '.cache' / 'fapcraft-cleanroom'))
    args = parser.parse_args()

    instance = Path(args.instance).expanduser()
    patch_path = instance / 'patches' / 'net.minecraftforge.json'
    pack_path = instance / 'mmc-pack.json'
    if not patch_path.is_file() or not pack_path.is_file():
        sys.exit(f'not a Cleanroom MMC instance: {instance}')

    patch = json.loads(patch_path.read_text())
    current = patch.get('version')
    if current == args.version:
        print(f'Cleanroom already {current}; nothing to update.')
        return

    cache = Path(args.cache).expanduser()
    cache.mkdir(parents=True, exist_ok=True)
    installer = cache / f'cleanroom-{args.version}-installer.jar'
    download(f'{RELEASES}/{args.version}/cleanroom-{args.version}-installer.jar', installer)

    with zipfile.ZipFile(installer) as archive:
        version_json = json.loads(archive.read('version.json'))

    by_name = {lib['name']: lib for lib in version_json['libraries']}

    def new_entry(prefix: str):
        matches = [name for name in by_name if name.startswith(prefix)]
        if not matches:
            sys.exit(f'missing {prefix} in {installer}')
        name = matches[0]
        return name, by_name[name]

    replacements = {}
    for prefix in ('com.cleanroommc:cleanroom:', 'com.cleanroommc:cleanmix:', 'com.cleanroommc:mixinextras-common:'):
        replacements[prefix] = new_entry(prefix)

    backup = cache / 'backups' / time.strftime('%Y%m%d-%H%M%S')
    backup.mkdir(parents=True, exist_ok=True)
    shutil.copy2(patch_path, backup / patch_path.name)
    shutil.copy2(pack_path, backup / pack_path.name)

    def artifact(new_name: str, entry: dict) -> dict:
        download_info = entry['downloads']['artifact']
        if new_name.startswith('com.cleanroommc:cleanroom:'):
            url = f'{MAVEN}/com/cleanroommc/cleanroom/{args.version}/cleanroom-{args.version}-universal.jar'
        else:
            url = download_info['url']
        return {
            'name': new_name,
            'downloads': {
                'artifact': {
                    'url': url,
                    'sha1': download_info['sha1'],
                    'size': download_info['size'],
                }
            },
        }

    seen = set()
    libraries = []
    for library in patch['libraries']:
        matched = next((prefix for prefix in replacements if library['name'].startswith(prefix)), None)
        if matched is None:
            libraries.append(library)
            continue
        seen.add(matched)
        libraries.append(artifact(*replacements[matched]))

    for prefix, replacement in replacements.items():
        if prefix not in seen:
            libraries.append(artifact(*replacement))

    patch['libraries'] = libraries
    patch['version'] = args.version
    patch_path.write_text(json.dumps(patch, indent=4) + '\n')

    pack = json.loads(pack_path.read_text())
    for component in pack.get('components', []):
        if component.get('uid') == 'net.minecraftforge':
            component['version'] = args.version
            component['cachedVersion'] = args.version
    pack_path.write_text(json.dumps(pack, indent=4) + '\n')

    print(f'updated {patch_path} and {pack_path} to {args.version}')
    print(f'backup: {backup}')


if __name__ == '__main__':
    main()
