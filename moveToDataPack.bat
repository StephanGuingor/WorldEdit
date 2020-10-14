
set /p WORLD=Enter the world name:
set /p SCHEMTATIC_NAME=Enter schematic name:
set MODS_DIR=C:\Users\Stephan\AppData\Roaming\.minecraft\saves\%WORLD%\datapacks
set SCHEMATIC_DIR=C:\Users\Stephan\AppData\Roaming\.minecraft\config\worldedit\schematics

mkdir %MODS_DIR%\holoPack\data\the_hand\functions\hologram
cp -r %SCHEMATIC_DIR%\%SCHEMTATIC_NAME% %MODS_DIR%\



