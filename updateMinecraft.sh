MODS_DIR="C:\Users\Stephan\AppData\Roaming\.minecraft\mods"
FABRIC_JAR="worldedit-fabric\build\libs\worldedit-fabric-mc1.16.3-7.2.0-SNAPSHOT-dist.jar"
cp $(FABRIC_JAR) $(MODS_DIR)


if [ $? -eq 0 ]
then
  echo File has been copied!
else
  echo "Paths in updateMinecraft.bat must be wrong!"
fi

