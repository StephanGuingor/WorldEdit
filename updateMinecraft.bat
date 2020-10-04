set MODS_DIR=C:\Users\Stephan\AppData\Roaming\.minecraft\mods
set FABRIC_JAR=worldedit-fabric\build\libs\worldedit-fabric-mc1.16.3-7.2.0-SNAPSHOT-dist.jar
cp %FABRIC_JAR% %MODS_DIR%

if %ERRORLEVEL% EQU 0 echo File has been copied!
if %ERRORLEVEL% EQU 1 echo Paths in updateMinecraft.bat must be wrong!
