import glob, re, sys
root = "<REPO>/src/main/java/com/trolmastercard/sexmod/"
files = []
for pat in ["client/gui/*.java","client/GirlCameraHelper.java","client/SexWorldClient.java","client/ShaderHelper.java","client/ClientNetHandlerOverride.java","GalathActionListener.java","PositionData.java","MatrixHelper.java","MobPredicates.java","SkinFetcher.java","SexNetworkManager.java","client/particle/DragonBreathParticle.java","item/*.java","command/*.java","worldgen/*.java","proxy/*.java","potion/HornyPotion.java","api/*.java","block/*.java","Main.java"]:
    files.extend(glob.glob(root + pat))
rx = re.compile(r'\bvar\d+[a-z_]?\d*\b')
bad = 0
for f in sorted(files):
    src = open(f).read()
    bal = src.count('{') - src.count('}')
    n = len(rx.findall(src))
    status = "OK" if bal == 0 and n == 0 else f"VAR {n} BRACE {bal}"
    if status != "OK":
        bad += 1
        print(f"{f.replace(root,'')}: {status}")
print(f"{len(files)} files checked, {bad} problems")
