src = open('<REPO>/src/main/java/com/trolmastercard/sexmod/client/renderer/GalathRenderer.java').read()
lines = src.split('\n')
# walk char by char tracking comment state
i = 0
n = len(src)
in_comment = False
comment_start = 0
while i < n - 1:
    if not in_comment and src[i] == '/' and src[i+1] == '*':
        in_comment = True
        comment_start = i
        i += 2
        continue
    if in_comment and src[i] == '*' and src[i+1] == '/':
        in_comment = False
        i += 2
        continue
    i += 1
print("ends in comment:", in_comment)
# find all javadoc starts and their closes using the same scanner, listing unterminated
i = 0
while i < n - 1:
    if src[i] == '/' and src[i+1] == '*':
        start = i
        i += 2
        closed = False
        while i < n - 1:
            if src[i] == '*' and src[i+1] == '/':
                closed = True
                i += 2
                break
            i += 1
        if not closed:
            print("UNTERMINATED at line", src.count('\n', 0, start) + 1)
    else:
        i += 1
