import os, sys

ROOT = '<REPO>/src/main/java/com/trolmastercard/sexmod'

no_javadoc = []
with_javadoc = []
for dirpath, dirs, files in os.walk(ROOT):
    for fn in sorted(files):
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dirpath, fn)
        with open(p, errors='replace') as f:
            content = f.read()
        (with_javadoc if '/**' in content else no_javadoc).append(os.path.relpath(p, ROOT))

print(f"TOTAL: {len(no_javadoc) + len(with_javadoc)}  with javadoc: {len(with_javadoc)}  without: {len(no_javadoc)}")
print("\n=== WITHOUT any javadoc ===")
for p in no_javadoc:
    print(p)
