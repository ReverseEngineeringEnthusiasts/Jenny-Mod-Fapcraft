import glob, re
pat = re.compile(r'\bvar[0-9]+[a-z_]?[0-9]*\b')
files = glob.glob('<REPO>/src/main/java/com/trolmastercard/sexmod/client/renderer/**/*.java', recursive=True) + glob.glob('<REPO>/src/main/java/com/trolmastercard/sexmod/client/model/**/*.java', recursive=True)
total = sum(len(pat.findall(open(f).read())) for f in files)
print("files:", len(files), "| varN occurrences remaining:", total)
