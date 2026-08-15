src = open('<REPO>/src/main/java/com/trolmastercard/sexmod/client/renderer/GalathRenderer.java').read()
i = 0
n = len(src)
in_comment = False
comment_start = 0
# collect all tokens with positions
tokens = []
while i < n - 1:
    if not in_comment and src[i] == '/' and src[i+1] == '*':
        in_comment = True
        comment_start = i
        tokens.append(('OPEN', i))
        i += 2
        continue
    if in_comment and src[i] == '*' and src[i+1] == '/':
        in_comment = False
        tokens.append(('CLOSE', i))
        i += 2
        continue
    i += 1
# stack match
stack = []
for t, p in tokens:
    if t == 'OPEN':
        stack.append(p)
    else:
        if stack:
            stack.pop()
        else:
            print("UNMATCHED CLOSE at line", src.count('\n', 0, p) + 1)
if stack:
    for p in stack:
        print("UNMATCHED OPEN at line", src.count('\n', 0, p) + 1)
