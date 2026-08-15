p = '<REPO>/src/main/java/com/trolmastercard/sexmod/networking/DownloadServerModelPacket.java'
s = open(p).read()
s = s.replace('Throwable var11;', 'Throwable caught;')
s = s.replace('} catch (Throwable caught) {\n                  caught = caught;', '} catch (Throwable exception) {\n                  caught = exception;')
open(p, 'w').write(s)
print('done')
