#!/usr/bin/env python3
"""Scope-aware varN renamer for CFR-decompiled Java.

Usage:
  vartool.py report <file>            # print scopes + varN occurrences with context
  vartool.py apply <file> <spec.json> # apply per-scope renames
  vartool.py check <file>             # print remaining varN (code vs string vs comment)
"""
import re, sys, json

VAR = re.compile(r'\bvar\d+[a-z_]?\d*\b')
KWCTRL = ('if', 'for', 'while', 'switch', 'catch', 'synchronized')

def is_control_block(pend):
    m = re.match(r'^\s*(else\s+)?(if|for|while|switch|catch|synchronized)\s*\(', pend)
    if not m:
        return False
    if not pend.rstrip().endswith(')'):
        return False
    depth = 0
    for ch in pend:
        if ch == '(':
            depth += 1
        elif ch == ')':
            depth -= 1
        if depth < 0:
            return False
    return depth == 0

def strip_comments(t):
    out = []
    i = 0
    n = len(t)
    while i < n:
        c = t[i]
        if c == '"':
            j = i + 1
            while j < n:
                if t[j] == '\\':
                    j += 2
                    continue
                if t[j] == '"':
                    break
                j += 1
            out.append(t[i:j+1])
            i = j + 1
        elif c == '/' and t[i+1:i+2] == '/':
            j = t.find('\n', i)
            if j == -1:
                j = n
            out.append(' ')
            i = j
        elif c == '/' and t[i+1:i+2] == '*':
            j = t.find('*/', i)
            if j == -1:
                j = n
            else:
                j += 2
            out.append(' ')
            i = j
        else:
            out.append(c)
            i += 1
    return ''.join(out)

def scan(src):
    n = len(src)
    i = 0
    state = 'code'
    stack = [{'kind': 'root', 'start': 0, 'sig_start': 0, 'end': n, 'children': []}]
    parens = 0
    last_delim = 0

    while i < n:
        c = src[i]
        if state == 'code':
            if c == '"':
                state = 'str'; i += 1; continue
            if c == "'":
                state = 'chr'; i += 1; continue
            if c == '/':
                nx = src[i+1:i+2]
                if nx == '/': state = 'line'; i += 2; continue
                if nx == '*': state = 'block'; i += 2; continue
            if c == '-' and src[i+1:i+2] == '>' and (i == 0 or src[i-1] != '-'):
                j = i + 2
                while j < n and src[j] in ' \t\r\n':
                    j += 1
                if not (j < n and src[j] == '{'):
                    k = i - 1
                    depth = 0
                    start = None
                    while k >= 0:
                        ch = src[k]
                        if ch == ')':
                            depth += 1
                        elif ch == '(':
                            if depth == 1:
                                start = k
                                break
                            elif depth > 1:
                                depth -= 1
                            else:
                                break
                        elif ch in ';{}':
                            break
                        k -= 1
                    if start is None:
                        k = i - 1
                        while k >= 0 and (src[k].isalnum() or src[k] in '_$'):
                            k -= 1
                        start = k + 1
                    depth = 0
                    e = i + 2
                    while e < n:
                        ch = src[e]
                        if ch == '"':
                            e += 1
                            while e < n:
                                if src[e] == '\\':
                                    e += 2
                                    continue
                                if src[e] == '"':
                                    break
                                e += 1
                            e += 1
                            continue
                        if ch == "'":
                            e += 1
                            while e < n:
                                if src[e] == '\\':
                                    e += 2
                                    continue
                                if src[e] == "'":
                                    break
                                e += 1
                            e += 1
                            continue
                        if ch in '([':
                            depth += 1
                        elif ch in ')]':
                            depth -= 1
                            if depth < 0:
                                break
                        elif ch == '{':
                            break
                        elif ch in ';,' and depth == 0:
                            break
                        e += 1
                    sc = {'kind': 'lambda', 'start': i + 2, 'sig_start': start, 'end': e, 'children': []}
                    stack[-1]['children'].append(sc)
                    i += 2
                    continue
            if c in '([':
                parens += 1
            elif c in ')]':
                parens -= 1
            elif c in ';{}':
                if c == '{':
                    pend = strip_comments(src[last_delim:i].rstrip())
                    pn = re.sub(r'\s+', '', pend)
                    if re.search(r'\b(class|interface|enum)\b', pend) or pn.startswith('@interface'):
                        kind = 'container'
                    elif re.search(r'\bnew\s+[\w.$]', pend) and (pend.endswith(')') or pend.endswith('>')):
                        kind = 'container'
                    elif '->' in pend:
                        kind = 'lambda'
                    elif re.search(r'[\w$]\s*\(', pend) and not is_control_block(pend):
                        kind = 'method'
                    elif stack[-1]['kind'] == 'container' and re.match(r'^(static|synchronized)?$', pn):
                        kind = 'init'
                    else:
                        kind = 'block'
                    sc = {'kind': kind, 'start': i + 1, 'sig_start': last_delim + 1,
                          'end': None, 'children': []}
                    stack[-1]['children'].append(sc)
                    stack.append(sc)
                elif c == '}':
                    if len(stack) > 1:
                        top = stack.pop()
                        top['end'] = i + 1
                        if top['kind'] == 'block':
                            top['kind'] = 'inherit'
                else:
                    if parens == 0:
                        last_delim = i + 1
                last_delim = i + 1
                pending_reset = True
                i += 1
                continue
            elif c == ';' and parens == 0:
                last_delim = i + 1
            i += 1
            continue
        if state == 'str':
            if c == '\\': i += 2; continue
            if c == '"': state = 'code'
            i += 1
            continue
        if state == 'chr':
            if c == '\\': i += 2; continue
            if c == "'": state = 'code'
            i += 1
            continue
        if state == 'line':
            if c == '\n': state = 'code'
            i += 1
            continue
        if state == 'block':
            if c == '*' and src[i+1:i+2] == '/': state = 'code'; i += 2; continue
            i += 1
            continue
    return stack[0]

def iter_scopes(sc, out=None):
    if out is None:
        out = []
    out.append(sc)
    for ch in sc['children']:
        iter_scopes(ch, out)
    return out

def code_ranges(src, start, end):
    ranges = []
    i, state = start, 'code'
    seg_start = None
    while i < end:
        c = src[i]
        if state == 'code':
            if seg_start is None:
                seg_start = i
            if c == '"':
                state = 'str'
            elif c == "'":
                state = 'chr'
            elif c == '/' and src[i+1:i+2] == '/':
                state = 'line'
            elif c == '/' and src[i+1:i+2] == '*':
                state = 'block'
            if state != 'code':
                ranges.append((seg_start, i))
                seg_start = None
            i += 1
            continue
        if state == 'str':
            if c == '\\': i += 2; continue
            if c == '"': state = 'code'
            i += 1
            continue
        if state == 'chr':
            if c == '\\': i += 2; continue
            if c == "'": state = 'code'
            i += 1
            continue
        if state == 'line':
            if c == '\n': state = 'code'
            i += 1
            continue
        if state == 'block':
            if c == '*' and src[i+1:i+2] == '/': state = 'code'; i += 2; continue
            i += 1
            continue
    if seg_start is not None:
        ranges.append((seg_start, end))
    return ranges

def line_of(src, off):
    return src.count('\n', 0, off) + 1

def sig_line(src, sc):
    return src[sc['sig_start']:sc['start']].replace('\n', ' ').strip()[-180:]

def find_var_usages(src, sc):
    usages = []
    for (a, b) in code_ranges(src, sc['start'], sc['end']):
        for m in VAR.finditer(src, a, b):
            usages.append((m.start(), m.group(0)))
    return usages

def report(src):
    root = scan(src)
    for sc in iter_scopes(root):
        if sc['kind'] in ('root', 'inherit', 'container'):
            continue
        usages = find_var_usages(src, sc)
        if not usages:
            continue
        from collections import Counter
        cnt = Counter(name for _, name in usages)
        print(f"== L{line_of(src, sc['sig_start'])} [{sc['kind']}] {sig_line(src, sc)}")
        seen = {}
        for off, name in usages:
            if name not in seen:
                seen[name] = line_of(src, off)
                ctx = src[off:off+75].split('\n')[0]
                print(f"    {name} (x{cnt[name]} @L{seen[name]}): {ctx}")

def descendant_spans(sc, out):
    for ch in sc['children']:
        if ch['kind'] in ('method', 'lambda', 'init', 'container'):
            out.append((ch['sig_start'], ch['end']))
            descendant_spans(ch, out)

def apply_decl(src, sig, renames):
    total = 0
    lines = src.split('\n')
    i = 0
    out = []
    while i < len(lines):
        norm = re.sub(r'\s+', ' ', lines[i]).strip()
        if re.search(sig, norm):
            j = i
            joined = ''
            while j < len(lines):
                joined += lines[j] + '\n'
                depth = 0
                in_str = False
                for ch in joined:
                    if ch == '"':
                        in_str = not in_str
                    elif not in_str and ch == '(':
                        depth += 1
                    elif not in_str and ch == ')':
                        depth -= 1
                if not in_str and depth <= 0 and joined.rstrip().endswith(';'):
                    break
                j += 1
            seg, cnt = re.subn(r'\bvar\d+[a-z_]?\d*\b',
                               lambda m: renames.get(m.group(0), m.group(0)), joined)
            total += cnt
            out.append(seg.rstrip('\n'))
            i = j + 1
        else:
            out.append(lines[i])
            i += 1
    return '\n'.join(out), total

def apply_comments(src, renames):
    total = 0
    out = []
    n = len(src)
    i = 0
    state = 'code'
    seg_start = 0
    while i < n:
        c = src[i]
        if state == 'code':
            if c == '"':
                out.append(src[seg_start:i])
                state = 'str'
            elif c == "'":
                out.append(src[seg_start:i])
                state = 'chr'
            elif c == '/' and src[i+1:i+2] == '/':
                out.append(src[seg_start:i])
                state = 'line'
                seg_start = i
            elif c == '/' and src[i+1:i+2] == '*':
                out.append(src[seg_start:i])
                state = 'block'
                seg_start = i
            i += 1
            continue
        if state == 'line':
            end = src.find('\n', i)
            if end == -1:
                end = n
            seg, cnt = re.subn(r'\bvar\d+[a-z_]?\d*\b',
                               lambda m: renames.get(m.group(0), m.group(0)), src[seg_start:end])
            out.append(seg)
            total += cnt
            i = end
            state = 'code'
            seg_start = end
            continue
        if state == 'block':
            end = src.find('*/', i)
            if end == -1:
                end = n
            else:
                end += 2
            seg, cnt = re.subn(r'\bvar\d+[a-z_]?\d*\b',
                               lambda m: renames.get(m.group(0), m.group(0)), src[seg_start:end])
            out.append(seg)
            total += cnt
            i = end
            state = 'code'
            seg_start = end
            continue
        if state == 'str':
            if c == '\\':
                i += 2
                continue
            if c == '"':
                state = 'code'
                seg_start = i + 1
            i += 1
            continue
        if state == 'chr':
            if c == '\\':
                i += 2
                continue
            if c == "'":
                state = 'code'
                seg_start = i + 1
            i += 1
            continue
    out.append(src[seg_start:])
    return ''.join(out), total

def apply(src, spec):
    errors = []
    total = 0
    for entry in spec['scopes']:
        sig, renames = entry['sig'], entry['renames']
        if entry.get('decl'):
            src, cnt = apply_decl(src, sig, renames)
            total += cnt
            continue
        if entry.get('cmt'):
            src, cnt = apply_comments(src, renames)
            total += cnt
            continue
        matched_any = 0
        while True:
            root = scan(src)
            found = None
            for sc in iter_scopes(root):
                if sc['kind'] not in ('method', 'lambda', 'init'):
                    continue
                region = re.sub(r'\s+', ' ', src[sc['sig_start']:sc['end']]).strip()
                if re.search(sig, region):
                    found = sc
                    break
            if found is None:
                break
            matched_any += 1
            sc = found
            start = sc['sig_start']
            excl = []
            descendant_spans(sc, excl)
            ranges = []
            for (a, b) in code_ranges(src, start, sc['end']):
                if any(x <= a and b <= y for (x, y) in excl):
                    continue
                ranges.append((a, b))
            idents = set()
            for (a, b) in ranges:
                idents.update(re.findall(r'[A-Za-z_$][\w$]*', src[a:b]))
            out = []
            pos = start
            scope_cnt = 0
            extra = entry.get('extra', {})
            for (a, b) in ranges:
                out.append(src[pos:a])
                seg, cnt = re.subn(r'\bvar\d+[a-z_]?\d*\b',
                                   lambda m: renames.get(m.group(0), m.group(0)), src[a:b])
                if extra:
                    pat = r'\b(?:' + '|'.join(re.escape(k) for k in extra) + r')\b'
                    seg, cnt2 = re.subn(pat, lambda m: extra.get(m.group(0), m.group(0)), seg)
                    cnt += cnt2
                out.append(seg)
                pos = b
                total += cnt
                scope_cnt += cnt
            out.append(src[pos:sc['end']])
            src = src[:start] + ''.join(out) + src[sc['end']:]
            if scope_cnt == 0:
                break
            for old, new in renames.items():
                if new in idents:
                    errors.append(f"collision: {new} already in scope {sig!r}")
        if matched_any == 0:
            errors.append(f"sig not found: {sig!r}")
    return src, total, errors

def check(src):
    out = []
    state = 'code'
    i = 0
    n = len(src)
    while i < n:
        c = src[i]
        m = VAR.match(src, i)
        if m:
            out.append((m.start(), state, m.group(0)))
            i = m.end()
            continue
        if state == 'code':
            if c == '"': state = 'str'
            elif c == "'": state = 'chr'
            elif c == '/' and src[i+1:i+2] == '/': state = 'line'
            elif c == '/' and src[i+1:i+2] == '*': state = 'block'
            i += 1
            continue
        if state == 'str':
            if c == '\\': i += 2; continue
            if c == '"': state = 'code'
            i += 1
            continue
        if state == 'chr':
            if c == '\\': i += 2; continue
            if c == "'": state = 'code'
            i += 1
            continue
        if state == 'line':
            if c == '\n': state = 'code'
            i += 1
            continue
        if state == 'block':
            if c == '*' and src[i+1:i+2] == '/': state = 'code'; i += 2; continue
            i += 1
            continue
    return out

def print_check(src):
    res = check(src)
    for kind in ('code', 'comment', 'string'):
        items = [(line_of(src, off), name, src[off:off+60].split('\n')[0]) for off, k, name in res if k == kind]
        print(f"--- {kind}: {len(items)}")
        for ln, name, ctx in items[:500]:
            print(f"  {ln}: {name}: {ctx}")

if __name__ == '__main__':
    mode = sys.argv[1]
    path = sys.argv[2]
    src = open(path).read()
    if mode == 'report':
        report(src)
    elif mode == 'check':
        print_check(src)
    elif mode == 'apply':
        spec = json.load(open(sys.argv[3]))
        newsrc, total, errors = apply(src, spec)
        open(path, 'w').write(newsrc)
        print(f"renamed {total} tokens")
        for e in errors:
            print("ERROR:", e)
