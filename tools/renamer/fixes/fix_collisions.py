import re

def fix_collisions(path, pairs):
    """pairs: list of (needle, newName) — find the needle line, rename its declared var + uses until the enclosing block ends (next 'case "' or 'break;' at same indent heuristic)."""
    s = open(path).read()
    lines = s.split('\n')
    for needle, new_name in pairs:
        found = None
        for i, line in enumerate(lines):
            if needle in line:
                found = i
                break
        assert found is not None, (path, needle)
        m = re.search(r'\b(\w+)\s*=\s*', lines[found].split(';')[0])
        old = m.group(1)
        lines[found] = lines[found].replace(old, new_name, 1)
        j = found + 1
        while j < len(lines) and 'case "' not in lines[j]:
            lines[j] = re.sub(r'\b' + re.escape(old) + r'\b', new_name, lines[j])
            j += 1
    open(path, 'w').write('\n'.join(lines))
    print('fixed', path)

base = '<REPO>/src/main/java/com/trolmastercard/sexmod/entity/'

# SlimeEntity 621 / SlimePlayerEntity 414 / JennyEntity 868 / JennyPlayerEntity 612:
# second 'int choice = Reference.RANDOM.nextInt(2)' inside same sound lambda
fix_collisions(base + 'SlimeEntity.java', [
    ('int choice = Reference.RANDOM.nextInt(2);', 'choice2'),
])
fix_collisions(base + 'SlimePlayerEntity.java', [
    ('int choice = Reference.RANDOM.nextInt(2);', 'choice2'),
])
fix_collisions(base + 'JennyEntity.java', [
    ('int choice = Reference.RANDOM.nextInt(2);', 'choice2'),
])
fix_collisions(base + 'JennyPlayerEntity.java', [
    ('int choice = Reference.RANDOM.nextInt(2);', 'choice2'),
])
# ElliePlayerEntity 406/431: two 'EntityPlayerSP player' in same lambda
fix_collisions(base + 'ElliePlayerEntity.java', [
    ('EntityPlayerSP player = Minecraft.getMinecraft().player;', 'localPlayer'),
])
