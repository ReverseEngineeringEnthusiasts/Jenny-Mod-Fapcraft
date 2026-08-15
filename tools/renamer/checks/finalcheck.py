import re, glob, sys
root = "<REPO>/src/main/java/com/trolmastercard/sexmod/"
files = []
for pat in ["client/gui/*.java","client/GirlCameraHelper.java","client/SexWorldClient.java","client/ShaderHelper.java","client/ClientNetHandlerOverride.java","GalathActionListener.java","PositionData.java","MatrixHelper.java","MobPredicates.java","SkinFetcher.java","SexNetworkManager.java","client/particle/DragonBreathParticle.java","item/*.java","command/*.java","worldgen/*.java","proxy/*.java","potion/HornyPotion.java","api/*.java","block/*.java","Main.java"]:
    files.extend(glob.glob(root + pat))
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'core'))
import vartool
rx = re.compile(r'\bvar\d+[a-z_]?\d*\b')
total = 0
for f in sorted(files):
    src = open(f).read()
    n = len(rx.findall(src))
    if n:
        total += n
        kinds = {}
        for off, k, name in vartool.check(src):
            kinds.setdefault(k, []).append(vartool.line_of(src, off))
        print(f"{f.replace(root,'')}: {n} varN  ({kinds})")
print("TOTAL REMAINING:", total)
