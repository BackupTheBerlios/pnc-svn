[Setup]
AppName=ImageView
AppVerName=ImageView version 1.0
DefaultDirName={pf}\ImageView
DefaultGroupName=ImageView
UninstallDisplayIcon={app}\imageview.exe
Compression=lzma
SolidCompression=yes
OutputDir=.
ChangesAssociations=yes

[Files]
Source: "bin/imageview.exe"; DestDir: "{app}"
Source: "../../../target/imageview-1.0-SNAPSHOT.jar"; DestDir: "{app}"
Source: "../../../../../common/draw-utils/target/draw-utils-1.0-SNAPSHOT.jar"; DestDir: "{app}"

[Icons]
Name: "{group}\ImageView"; Filename: "{app}\imageview.exe"; WorkingDir: "{app}"

[Registry]
Root: HKCR; Subkey: ".jpg"; ValueType: string; ValueName: ""; ValueData: "ImageView"; Flags: uninsdeletevalue
Root: HKCR; Subkey: ".gif"; ValueType: string; ValueName: ""; ValueData: "ImageView"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "ImageView"; ValueType: string; ValueName: ""; ValueData: "ImageView"; Flags: uninsdeletekey
Root: HKCR; Subkey: "ImageView\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\imageview.exe,0"
Root: HKCR; Subkey: "ImageView\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\imageview.exe"" ""%1"""

