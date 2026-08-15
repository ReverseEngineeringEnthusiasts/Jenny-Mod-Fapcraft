import gzip, os, re, sys

base = "<HOME>/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/More FPS [FORGE] - Increase FPS & Shaders (1.21 UPDATE)/minecraft/logs"
pat = re.compile(r'removed corrupt|CHAT|tribe of UUID|Exception|nobed|joined the game|Saving and pausing|logged in|Touch')
files = sorted(f for f in os.listdir(base) if re.match(r'2026-08-1[34]-\d+\.log\.gz', f))
print("files:", len(files))
for fn in files:
    print("##### " + fn)
    with gzip.open(os.path.join(base, fn), 'rt', errors='replace') as f:
        for line in f:
            if pat.search(line):
                print(line.rstrip()[:200])