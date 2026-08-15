import re, glob

def find_matching_brace(text, open_idx):
    depth = 0
    for i in range(open_idx, len(text)):
        c = text[i]
        if c == '{':
            depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0:
                return i
    return -1

files = sorted(glob.glob('<REPO>/src/main/java/com/trolmastercard/sexmod/client/renderer/**/*.java', recursive=True)
             + glob.glob('<REPO>/src/main/java/com/trolmastercard/sexmod/client/model/**/*.java', recursive=True))

# find method units: any '{' preceded by ')' on same line-ish (signature), from javadoc to matching close
decl_re = re.compile(r'\b(?:final|static|public|protected|private|abstract|synchronized|\@\w+)*\s*([A-Z][\w$<>.,?\[\] ]*)\s+([a-z]\w*)\s*(?:=|;|,|\))')

issues = 0
for f in files:
    text = open(f).read()
    # candidate method starts: positions of '{' whose preceding non-space char on the same line is ')'
    for m in re.finditer(r'\)\s*\{', text):
        open_idx = m.start() + 1
        close_idx = find_matching_brace(text, open_idx)
        if close_idx < 0:
            continue
        # exclude anonymous class bodies: the '{' is preceded by 'new X(...) {' or ')' of a lambda
        before = text[m.start()-1]
        # crude filter: method signature must contain ')' followed by '{' and the '(' has a '(' before it on the same line with a name
        line = text[:m.start()].rfind('\n')
        sigline = text[line+1:m.start()]
        if not re.search(r'[a-z]\w*\s*\(', sigline) or 'new ' in sigline:
            continue
        unit = text[open_idx:close_idx]
        # collect declared names: type + name pairs
        declared = {}
        for dm in decl_re.finditer(unit):
            name = dm.group(2)
            # strip generics from type
            t = re.sub(r'<.*>', '', dm.group(1)).strip().split()[-1]
            declared.setdefault(name, set()).add(t)
        dups = {n: ts for n, ts in declared.items() if len(ts) > 1}
        if dups:
            print(f, 'line', text[:open_idx].count('\n')+1, 'DUPLICATE DECLS:', dups)
            issues += 1
print('units with potential duplicate declarations:', issues)
