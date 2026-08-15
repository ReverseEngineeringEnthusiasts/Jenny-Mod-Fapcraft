import glob, re, sys
root = "<REPO>/src/main/java/com/trolmastercard/sexmod/"
files = []
for pat in ["client/gui/*.java","client/GirlCameraHelper.java","client/SexWorldClient.java","client/ShaderHelper.java","client/ClientNetHandlerOverride.java","GalathActionListener.java","PositionData.java","MatrixHelper.java","MobPredicates.java","SkinFetcher.java","SexNetworkManager.java","client/particle/DragonBreathParticle.java","item/*.java","command/*.java","worldgen/*.java","proxy/*.java","potion/HornyPotion.java","api/*.java","block/*.java","Main.java"]:
    files.extend(glob.glob(root + pat))
rx = re.compile(r'\bvar\d+[a-z_]?\d*\b')
rows = []
for f in files:
    src = open(f).read()
    n = len(rx.findall(src))
    rows.append((n, src.count("\n"), f.replace(root, "")))
for n, l, f in sorted(rows, reverse=True):
    print(f"{n:5d} {l:5d} {f}")
print("TOTAL FILES:", len(rows), "TOTAL varN:", sum(r[0] for r in rows))
