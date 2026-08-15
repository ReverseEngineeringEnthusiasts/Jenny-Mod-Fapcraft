import subprocess, glob, os, re

# find every original class calling b6.a(Vec3d, Vec3d, I) — the int/divide lerp
base = '/tmp/orig/jar/com/trolmastercard/sexmod'
pat_i = re.compile(r'Method com/trolmastercard/sexmod/b6\.a:\(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;I\)Lnet/minecraft/util/math/Vec3d;')
pat_d = re.compile(r'Method com/trolmastercard/sexmod/b6\.a:\(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;D\)Lnet/minecraft/util/math/Vec3d;')
for p in sorted(glob.glob(base + '/*.class')):
    if b'b6' not in open(p, 'rb').read():
        continue
    out = subprocess.run(['javap', '-c', '-p', p], capture_output=True, text=True).stdout
    ni = len(pat_i.findall(out))
    nd = len(pat_d.findall(out))
    if ni or nd:
        print(os.path.basename(p), 'INT:', ni, 'DOUBLE:', nd)