import sys
sys.path.insert(0, '/tmp')
src = open(sys.argv[1]).read()
n = len(src)
i = 0
state = 'code'
while i < n:
    c = src[i]
    if state == 'code':
        if c == '"': state = 'str'; i += 1; continue
        if c == "'": state = 'chr'; i += 1; continue
        if c == '/':
            nx = src[i+1:i+2]
            if nx == '/': state = 'line'; i += 2; continue
            if nx == '*': state = 'block'; i += 2; continue
        if c in '{}':
            print(f"{i:5d} L{src.count(chr(10),0,i)+1} {c!r} state={state}")
        i += 1
        continue
    if state == 'str':
        if c == '\\': i += 2; continue
        if c == '"': state = 'code'
        i += 1; continue
    if state == 'chr':
        if c == '\\': i += 2; continue
        if c == "'": state = 'code'
        i += 1; continue
    if state == 'line':
        if c == '\n': state = 'code'
        i += 1; continue
    if state == 'block':
        if c == '*' and src[i+1:i+2] == '/': state = 'code'; i += 2; continue
        i += 1; continue
