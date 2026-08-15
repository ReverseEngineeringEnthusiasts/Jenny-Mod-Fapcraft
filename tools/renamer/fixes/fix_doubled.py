import os, re

ROOT = '<REPO>/src/main/java/com/trolmastercard/sexmod'

fixed = 0
for dirpath, dirs, files in os.walk(ROOT):
    for fn in files:
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dirpath, fn)
        s = open(p).read()
        orig = s
        # collapse doubled javadoc opener: "/**\n/**" -> "/**"
        s = re.sub(r'/\*\*\n/\*\*', '/**', s)
        # collapse doubled closer: " */\n */" -> " */"
        s = re.sub(r' \*/\n \*/', ' */', s)
        if s != orig:
            open(p, 'w').write(s)
            fixed += 1
print("fixed files:", fixed)
