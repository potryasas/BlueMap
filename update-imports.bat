@echo off
echo Updating imports in command files...

set CMD_DIR=common\src\main\java\de\bluecolored\bluemap\common\commands\commands

cd /d %~dp0
if not exist %CMD_DIR% (
    echo Error: Commands directory not found!
    exit /b 1
)

powershell -Command "Get-ChildItem -Path '%CMD_DIR%' -Filter '*.java' | ForEach-Object { (Get-Content $_.FullName) -replace 'import de\.bluecolored\.bluecommands\.annotations\.', 'import de.bluecolored.bluemap.common.commands.java8compat.annotations.' | Set-Content $_.FullName }"

powershell -Command "Get-ChildItem -Path '%CMD_DIR%' -Filter '*.java' | ForEach-Object { (Get-Content $_.FullName) -replace 'import de\.bluecolored\.bluecommands\.parsers\.SimpleArgumentParser', 'import de.bluecolored.bluemap.common.commands.java8compat.SimpleArgumentParser' | Set-Content $_.FullName }"

powershell -Command "Get-ChildItem -Path '%CMD_DIR%' -Filter '*.java' | ForEach-Object { (Get-Content $_.FullName) -replace 'import de\.bluecolored\.bluecommands\.CommandParseException', 'import de.bluecolored.bluemap.common.commands.java8compat.SimpleArgumentParser.CommandParseException' | Set-Content $_.FullName }"

powershell -Command "Get-ChildItem -Path '%CMD_DIR%' -Filter '*.java' | ForEach-Object { (Get-Content $_.FullName) -replace 'import de\.bluecolored\.bluecommands\.InputReader', 'import de.bluecolored.bluemap.common.commands.java8compat.SimpleArgumentParser.InputReader' | Set-Content $_.FullName }"

powershell -Command "Get-ChildItem -Path '%CMD_DIR%' -Filter '*.java' | ForEach-Object { (Get-Content $_.FullName) -replace 'import de\.bluecolored\.bluecommands\.Suggestion', 'import de.bluecolored.bluemap.common.commands.java8compat.SimpleArgumentParser.Suggestion' | Set-Content $_.FullName }"

powershell -Command "Get-ChildItem -Path '%CMD_DIR%' -Filter '*.java' | ForEach-Object { (Get-Content $_.FullName) -replace 'import de\.bluecolored\.bluecommands\.SimpleSuggestion', 'import de.bluecolored.bluemap.common.commands.java8compat.SimpleArgumentParser.Suggestion' | Set-Content $_.FullName }"

echo Import updates complete! 