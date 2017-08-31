Pechkin.v0.3.1

Copyright 2017 Fenenko Aleksandr.

Licensed Apache License, Version 2.0;

This is lightweight bitmessage client writing in java.

Change:
	add create windows executable programm
Fix:
	error not create poppup menu in addressTree
	not sent broadcast message

This is desktop version Pechkin.
Repositories android version - http://hg.code.sf.net/p/pechkin/android
	






Address to contact the developer: BM-2cT9H4ow7R35qLhcsiTNxFjTpnx44XTbiY


INSTALL(linux):
install java JRE
download https://sourceforge.net/projects/pechkin/files/Pechkin_v.0.3.1.zip/download
unpack Pechkin v.0.3.1.zip
open unpack directory and run programm:
run.sh  (for linux)

INSTALL(windows)
download https://sourceforge.net/projects/pechkin/files/Pechkin_v.0.3.1.win32.zip/download
unpack 

BUILD:
hg clone http://hg.code.sf.net/p/pechkin/code pechkin-code
cd pechkin-code
ant
CREATE WINDOWS EXE FILE:
download launch4j(http://launch4j.sourceforge.net/)
unpack to pechkin-code
ant exe


