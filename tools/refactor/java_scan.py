"""Shared Java scanning helpers for the refactor tools.

`mask_java` blanks comments, string literals and char literals while preserving
offsets. Char literals matter: `c != '"'` otherwise looks like the start of a
string and swallows the rest of the file.
"""
import re


def mask_java(text):
    out = list(text)
    i, n = 0, len(text)
    while i < n:
        c = text[i]
        if c == '"':
            start = i
            i += 1
            while i < n:
                if text[i] == '\\':
                    i += 2
                    continue
                if text[i] == '"':
                    i += 1
                    break
                i += 1
            for k in range(start, min(i, n)):
                out[k] = ' '
            continue
        if c == "'":
            start = i
            i += 1
            while i < n:
                if text[i] == '\\':
                    i += 2
                    continue
                if text[i] == "'":
                    i += 1
                    break
                i += 1
            for k in range(start, min(i, n)):
                out[k] = ' '
            continue
        if c == '/' and i + 1 < n and text[i + 1] == '/':
            while i < n and text[i] != '\n':
                out[i] = ' '
                i += 1
            continue
        if c == '/' and i + 1 < n and text[i + 1] == '*':
            while i + 1 < n and not (text[i] == '*' and text[i + 1] == '/'):
                out[i] = ' '
                i += 1
            if i + 1 < n:
                out[i] = out[i + 1] = ' '
                i += 2
            continue
        i += 1
    return ''.join(out)


def package_of(text):
    m = re.search(r'^package\s+([\w.]+);', text, re.M)
    return m.group(1) if m else ''
