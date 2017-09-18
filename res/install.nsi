
OutFile "pechkin install.v.0.3.2.exe"

SetCompressor lzma


InstallDir $DESKTOP\pechkin

Page License 
LicenseText "Pechkin (http://pechkin.sourceforge.io) opensource programm"
LicenseData ./LICENSE.installer.txt
Page Directory
Page InstFiles

 
# default section
Section
 
# define the output path for this file
SetOutPath $INSTDIR

File ../dist/Pechkin.exe
File ../dist/LICENSE.txt
File ../dist/README.txt
SectionEnd


Section
SetOutPath $INSTDIR\lib
File ../dist/lib/protobuf.jar
File ../dist/lib/LICENSE.protobuf.txt

SectionEnd
Section
SetOutPath $INSTDIR\lib
File ../dist/lib/bcprov-jdk15on-156.jar
File ../dist/lib/LICENSE.bcprov.txt
SectionEnd
Section
WriteUninstaller $INSTDIR\uninstaller.exe
SectionEnd

Section
 
    # create a shortcut named "new shortcut" in the start menu programs directory
    # presently, the new shortcut doesn't call anything (the second field is blank)
    createShortCut "$SMPROGRAMS\Pechkin.lnk" "$INSTDIR\Pechkin.exe"
 
    # to delete shortcut, go to start menu directory and manually delete it
 
# default sec end
SectionEnd

Section "Uninstall"
 
# Always delete uninstaller first
Delete $INSTDIR\uninstaller.exe
 
# now delete installed file
Delete $INSTDIR\lib\bcprov-jdk15on-156.jar
Delete $INSTDIR\lib\protobuf.jar
Delete $INSTDIR\lib\LICENSE.protobuf.txt
Delete $INSTDIR\lib\LICENSE.bcprov.txt
Delete $INSTDIR\lib
Delete $INSTDIR\Pechkin.exe
Delete $INSTDIR\README.exe
Delete $SMPROGRAMS\Pechkin.lnk
 
SectionEnd


