#!/usr/bin/env python3
"""Scope-aware varN renamer for decompiled Java (CFR-style var1/var2x/var3_1).

Usage: rename_varN.py <file.java> <mapping.json>

Mapping JSON:
{
  "methods": {
     "<signature or name>": {"var1": "newName", ...}
  },
  "lambdas": {
     "<methodKey>#<ordinal>": {"var0": "name", ...}
  },
  "init": {"var1": "name"},        # static/instance initializer blocks
  "global": {"var1": "name"}       # fallback for unowned scopes
}
Method key may be full signature "name(Type1,Type2)" or bare name.
"""
import json, keyword, re, sys

VAR_RE = re.compile(r'\bvar(\d+)([a-z_]?\d*)\b')
KEYWORDS = {'if', 'while', 'for', 'catch', 'switch', 'synchronized', 'do',
            'else', 'return', 'new', 'throw', 'try', 'case', 'assert'}

# ---------------- lexer ----------------
def lex(src):
    toks = []  # (kind, text, start, end)
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c.isspace():
            i += 1
        elif c.isalpha() or c == '_' or c == '$':
            j = i
            while j < n and (src[j].isalnum() or src[j] in '_$'):
                j += 1
            toks.append(('ID', src[i:j], i, j))
            i = j
        elif c.isdigit():
            j = i
            while j < n and (src[j].isalnum() or src[j] in '._'):
                j += 1
            toks.append(('NUM', src[i:j], i, j))
            i = j
        elif c == '"':
            j = i + 1
            while j < n and src[j] != '"':
                if src[j] == '\\':
                    j += 2
                else:
                    j += 1
            j = min(j + 1, n)
            toks.append(('STR', src[i:j], i, j))
            i = j
        elif c == "'":
            j = i + 1
            while j < n and src[j] != "'":
                if src[j] == '\\':
                    j += 2
                else:
                    j += 1
            j = min(j + 1, n)
            toks.append(('CHAR', src[i:j], i, j))
            i = j
        elif c == '/' and i + 1 < n and src[i + 1] == '/':
            j = src.find('\n', i)
            if j == -1:
                j = n
            toks.append(('COMMENT', src[i:j], i, j))
            i = j
        elif c == '/' and i + 1 < n and src[i + 1] == '*':
            j = src.find('*/', i + 2)
            j = n if j == -1 else j + 2
            toks.append(('COMMENT', src[i:j], i, j))
            i = j
        else:
            # multi-char operators we care about
            two = src[i:i + 2]
            if two in ('->', '::', '==', '!=', '<=', '>=', '&&', '||', '<<', '>>', '+=', '-=', '*=', '/=', '%=', '&=', '|=', '^=', '++', '--', '..'):
                toks.append(('OP', two, i, i + 2))
                i += 2
            else:
                toks.append(('OP', c, i, i + 1))
                i += 1
    return toks

# ---------------- structural scan ----------------
def matching(toks, k, open_ch, close_ch):
    """Given token index k points at open_ch, return index of matching close_ch."""
    depth = 0
    for j in range(k, len(toks)):
        if toks[j][1] == open_ch:
            depth += 1
        elif toks[j][1] == close_ch:
            depth -= 1
            if depth == 0:
                return j
    return None

def find_paren_end(toks, k):
    """k points at '('. Return index of matching ')'."""
    return matching(toks, k, '(', ')')

def scope_scan(toks):
    """Find method/lambda/init scopes. Returns (scopes, errors).
    scope: dict(name, sig, params[varN...], body_start, body_end, kind,
                lambda_ordinal, method_key, parent_method)
    """
    scopes = []
    errors = []
    stack = []  # frames: {'kind':..., 'method_ref': scope-dict-or-None}
    i = 0
    n = len(toks)
    while i < n:
        kind, text, s, e = toks[i]
        if text == '{':
            # possibly static/instance init block
            prev = toks[i - 1][1] if i > 0 else ''
            if prev == 'static':
                body_end = matching(toks, i, '{', '}')
                scopes.append({'kind': 'init', 'name': 'init', 'sig': 'init',
                               'params': [], 'body_start': i, 'body_end': body_end})
                stack.append({'kind': 'initbody', 'method_ref': stack[-1].get('method_ref') if stack else None})
                i = body_end + 1
                continue
            stack.append({'kind': 'block', 'method_ref': stack[-1].get('method_ref') if stack else None})
        elif text == '}':
            if stack:
                stack.pop()
        elif text == '(':
            close = find_paren_end(toks, i)
            if close is None:
                errors.append(f'line? unmatched ( at tok {i}')
                i += 1
                continue
            prev_tok = toks[i - 1][1] if i > 0 else ''
            # what follows the ) ?
            j = close + 1
            while j < n and toks[j][1] in ('.', '['):
                j += 1
            # skip throws clause
            k = j
            if k < n and toks[k][1] == 'throws':
                k += 1
                while k < n and toks[k][1] != '{':
                    k += 1
            if k < n and toks[k][1] == '{':
                # paren-group directly followed by a block
                if prev_tok in ('if', 'while', 'for', 'catch', 'switch', 'synchronized'):
                    stack.append({'kind': 'block', 'method_ref': stack[-1].get('method_ref') if stack else None})
                    i = k + 1
                    continue
                # anonymous class: 'new' before type before paren
                t2 = toks[i - 2][1] if i >= 2 else ''
                if prev_tok == 'new' or t2 == 'new':
                    # anonymous class body: contains methods; treat as container block
                    body_end = matching(toks, k, '{', '}')
                    stack.append({'kind': 'classbody', 'method_ref': None})
                    i = k + 1
                    continue
                # enum constant with body: IDENT ( args ) { ... }
                if prev_tok == '}' or (prev_tok.isidentifier() and not keyword.iskeyword(prev_tok)
                                       and i >= 2 and toks[i - 2][1] not in ('new', 'class')):
                    # method/constructor declaration
                    name = prev_tok if prev_tok.isidentifier() and not keyword.iskeyword(prev_tok) else '?'
                    # params: depth-0 idents immediately before , or ) or end
                    params = []
                    pdepth = 0
                    adepth = 0
                    for p in range(i + 1, close + 1):
                        pt = toks[p][1]
                        if pdepth == 0 and adepth == 0:
                            if pt in (',', ')'):
                                # last ID before , or ) is the param name
                                q = p - 1
                                while q > i and toks[q][0] == 'COMMENT':
                                    q -= 1
                                if q > i and toks[q][0] == 'ID' and VAR_RE.match(toks[q][1]):
                                    params.append(toks[q][1])
                            elif pt == '.':
                                # varargs '...' or qualified names: the ident before . is a type
                                q = p - 1
                                while q > i and toks[q][0] == 'COMMENT':
                                    q -= 1
                                if q > i and toks[q][0] == 'ID' and VAR_RE.match(toks[q][1]):
                                    params.append(toks[q][1])
                        if pt == '(':
                            pdepth += 1
                        elif pt == ')':
                            pdepth -= 1
                            if pdepth < 0:
                                pdepth = 0
                        elif pt == '<':
                            adepth += 1
                        elif pt == '>':
                            adepth = max(0, adepth - 1)
                    # param types for signature
                    ptypes = []
                    pdepth = adepth = 0
                    cur = []
                    for p in range(i + 1, close + 1):
                        pt = toks[p]
                        if pdepth == 0 and adepth == 0 and pt[1] in (',', ')'):
                            if cur:
                                if VAR_RE.match(cur[-1]):
                                    cur = cur[:-1]
                                ptypes.append(''.join(cur))
                                cur = []
                        elif pt[1] == '(':
                            pdepth += 1
                            cur.append('(')
                        elif pt[1] == ')':
                            pdepth -= 1
                            if pdepth < 0:
                                pdepth = 0
                            cur.append(')')
                        elif pt[1] == '<':
                            adepth += 1
                            cur.append('<')
                        elif pt[1] == '>':
                            adepth = max(0, adepth - 1)
                            cur.append('>')
                        elif pdepth == 0 and adepth == 0:
                            cur.append(pt[1])
                        else:
                            cur.append(pt[1])
                    if cur:
                        if VAR_RE.match(cur[-1]):
                            cur = cur[:-1]
                        ptypes.append(''.join(cur))
                    body_end = matching(toks, k, '{', '}')
                    mref = stack[-1].get('method_ref') if stack else None
                    scopes.append({'kind': 'method', 'name': name, 'sig': f'{name}({",".join(ptypes)})',
                                   'params': params, 'body_start': i, 'body_end': body_end,
                                   'parent': mref})
                    # absorb immediately-preceding comment block (javadoc) into the region
                    j = i - 1
                    while j >= 0:
                        t = toks[j]
                        if t[0] == 'ID' or t[1] in ('@', '.', '<', '>', '[', ']'):
                            j -= 1
                        elif t[0] == 'COMMENT':
                            j -= 1
                        elif t[1] == ')':
                            depth = 0
                            kk = j
                            while kk >= 0:
                                if toks[kk][1] == ')':
                                    depth += 1
                                elif toks[kk][1] == '(':
                                    depth -= 1
                                    if depth == 0:
                                        break
                                kk -= 1
                            j = kk - 1
                        else:
                            break
                    if j >= -1 and j < i:
                        scopes[-1]['body_start'] = min(scopes[-1]['body_start'], j + 1)
                    stack.append({'kind': 'methodbody', 'method_ref': scopes[-1]})
                    i = k + 1
                    continue
                i = k + 1
                continue
            elif k < n and toks[k][1] == '->':
                # lambda params: idents in paren group at depth 0 before , or )
                lparams = []
                for p in range(i + 1, close + 1):
                    if toks[p][1] in (',', ')'):
                        q = p - 1
                        while q > i and toks[q][0] == 'COMMENT':
                            q -= 1
                        if q > i and toks[q][0] == 'ID' and VAR_RE.match(toks[q][1]):
                            lparams.append(toks[q][1])
                mref = stack[-1].get('method_ref') if stack else None
                if k + 1 < n and toks[k + 1][1] == '{':
                    # lambda with block body
                    body_start = k + 1
                    body_end = matching(toks, body_start, '{', '}')
                    scopes.append({'kind': 'lambda', 'name': None, 'sig': None,
                                   'params': lparams, 'body_start': i, 'body_end': body_end,
                                   'expr': False, 'parent': mref})
                    i = body_end + 1
                    continue
                else:
                    # expression lambda: region from ( through end of expression
                    end = k + 1
                    depth = 0
                    while end < n:
                        t = toks[end][1]
                        if t in ('(', '['):
                            depth += 1
                        elif t in (')', ']'):
                            if depth == 0:
                                break
                            depth -= 1
                        elif depth == 0 and t in (';', ',', '}', '{'):
                            break
                        end += 1
                    scopes.append({'kind': 'lambda', 'name': None, 'sig': None,
                                   'params': lparams, 'body_start': i, 'body_end': end,
                                   'expr': True, 'parent': mref})
                    i = end
                    continue
        elif text == '->':
            # lambda without parens: IDENT -> expr | IDENT -> { ... }
            prev_tok = toks[i - 1]
            lparams = []
            start = i - 1
            if prev_tok[0] == 'ID' and i >= 2 and toks[i - 2][1] not in ('ID', ')'):
                lparams = [prev_tok[1]]
            mref = stack[-1].get('method_ref') if stack else None
            if i + 1 < n and toks[i + 1][1] == '{':
                # block lambda: IDENT -> { ... }
                body_end = matching(toks, i + 1, '{', '}')
                scopes.append({'kind': 'lambda', 'name': None, 'sig': None, 'params': lparams,
                               'body_start': start, 'body_end': body_end, 'expr': False, 'parent': mref})
                i = body_end + 1
                continue
            # expression lambda: region from IDENT through end of expression
            end = i + 1
            depth = 0
            while end < n:
                t = toks[end][1]
                if t in ('(', '['):
                    depth += 1
                elif t in (')', ']'):
                    if depth == 0:
                        break
                    depth -= 1
                elif depth == 0 and t in (';', ',', '}', '{'):
                    break
                end += 1
            scopes.append({'kind': 'lambda', 'name': None, 'sig': None, 'params': lparams,
                           'body_start': start, 'body_end': end, 'expr': True, 'parent': mref})
            i = end
            continue
        i += 1
    return scopes, errors

def method_key(sc, mappings):
    sig = sc.get('sig') or sc.get('name') or '?'
    if sig in mappings.get('methods', {}):
        return sig
    if sc.get('name') and sc['name'] in mappings.get('methods', {}):
        return sc['name']
    return sig

# ---------------- renaming ----------------
def apply(src, toks, scopes, mappings, errors):
    methods_map = mappings.get('methods', {})
    lambdas_map = mappings.get('lambdas', {})
    init_map = mappings.get('init', {})
    global_map = mappings.get('global', {})

    # compute mapping per scope
    for sc in scopes:
        if sc['kind'] == 'method':
            key = method_key(sc, mappings)
            sc['key'] = key
            sc['map'] = methods_map.get(key, {})
            sc['lambdas_seen'] = 0
        elif sc['kind'] == 'init':
            sc['key'] = 'init'
            sc['map'] = init_map
            sc['lambdas_seen'] = 0
        else:
            sc['key'] = None
            sc['map'] = {}

    # for lambdas: parent map lookup by ordinal
    for sc in scopes:
        if sc['kind'] == 'lambda':
            p = sc['parent']
            if p is not None and p['kind'] in ('method', 'init'):
                key = p['key'] + '#' + str(p['lambdas_seen'])
                p['lambdas_seen'] += 1
                sc['key'] = key
                sc['map'] = lambdas_map.get(key, {})
                # error: lambda param collides with enclosing mapping but no own map
                for pname in sc['params']:
                    if pname in p['map'] and pname not in sc['map']:
                        errors.append(f"lambda {key} param {pname} collides with enclosing map "
                                      f"({pname}->{p['map'][pname]}) - need explicit lambda mapping")
                # propagate: lambda with no own map inherits parent's map? no -
                # captured vars handled by innermost-scope-with-binding rule.
            else:
                sc['key'] = 'lambda?'
                sc['map'] = {}

    # token -> innermost scope (by region size) whose map binds the varN
    def rename_for_token(tok_idx):
        kind, text, s, e = toks[tok_idx]
        m = VAR_RE.match(text)
        if not m:
            return None
        var = m.group(0)
        best = None
        for sc in scopes:
            rng = (sc['body_start'], sc['body_end'])
            if rng[0] is not None and rng[1] is not None and rng[0] <= tok_idx <= rng[1]:
                if var in sc['map']:
                    if best is None or (rng[1] - rng[0]) < (best[1] - best[0]):
                        best = (sc, rng)
        if best is not None:
            return best[0]['map'][var]
        if var in global_map:
            return global_map[var]
        return None

    out = []
    last = 0
    renamed = 0
    for idx, (kind, text, s, e) in enumerate(toks):
        if kind == 'STR':
            continue
        if kind == 'ID':
            new = rename_for_token(idx)
            if new:
                if new == text:
                    errors.append(f'no-op rename {text} -> {new} at tok {idx}')
                out.append(src[last:s])
                out.append(new)
                last = e
                renamed += 1
        elif kind == 'COMMENT':
            def sub(m):
                nonlocal renamed
                tok_idx = idx
                var = m.group(0)
                best = None
                for sc in scopes:
                    rng = (sc['body_start'], sc['body_end'])
                    if rng[0] is not None and rng[1] is not None and rng[0] <= tok_idx <= rng[1]:
                        if var in sc['map']:
                            if best is None or (rng[1] - rng[0]) < (best[1] - best[0]):
                                best = (sc, rng)
                new = best[0]['map'][var] if best else (global_map.get(var) if var in global_map else None)
                if new and new != var:
                    renamed += 1
                    return new
                return var
            newtext = VAR_RE.sub(sub, text)
            if newtext != text:
                out.append(src[last:s])
                out.append(newtext)
                last = e
    out.append(src[last:])
    return ''.join(out), renamed

def brace_balance(src):
    depth = 0
    in_str = in_char = False
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if in_str:
            if c == '\\':
                i += 2
                continue
            if c == '"':
                in_str = False
        elif in_char:
            if c == '\\':
                i += 2
                continue
            if c == "'":
                in_char = False
        else:
            if c == '"':
                in_str = True
            elif c == "'":
                in_char = True
            elif c == '/' and i + 1 < n and src[i + 1] == '/':
                j = src.find('\n', i)
                i = n if j == -1 else j
                continue
            elif c == '/' and i + 1 < n and src[i + 1] == '*':
                j = src.find('*/', i + 2)
                i = n if j == -1 else j + 2
                continue
            elif c == '{':
                depth += 1
            elif c == '}':
                depth -= 1
        i += 1
    return depth

def main():
    path, map_path = sys.argv[1], sys.argv[2]
    src = open(path, encoding='utf-8').read()
    mappings = json.load(open(map_path, encoding='utf-8'))
    toks = lex(src)
    scopes, errors = scope_scan(toks)
    before = brace_balance(src)
    out, renamed = apply(src, toks, scopes, mappings, errors)
    after = brace_balance(out)
    leftover = [(t[0] or '?', t[1]) for t in toks if VAR_RE.match(t[1]) and not rename_for_token(t[0])] if False else None
    print(f'{path}: scopes={len(scopes)} renamed={renamed} braces {before}->{after}')
    for err in errors:
        print(f'  ERROR: {err}')
    if errors:
        sys.exit(2)
    if before != after:
        print('  BRACE MISMATCH - aborting write')
        sys.exit(3)
    open(path, 'w', encoding='utf-8').write(out)

if __name__ == '__main__':
    main()
