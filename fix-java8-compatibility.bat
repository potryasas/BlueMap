@echo off
echo BlueMap Java 8 Compatibility Fixer
echo ==================================
echo.

set SRC_DIR=.
if not "%~1"=="" (
    set SRC_DIR=%~1
)

echo Scanning source directory: %SRC_DIR%
echo.

REM Check if PowerShell is available
where powershell >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: PowerShell is required for this script.
    exit /b 1
)

echo Fixing Path.of() calls...
powershell -Command "Get-ChildItem -Path '%SRC_DIR%' -Filter '*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace 'Path\.of\(', 'Paths.get(' | Set-Content $_.FullName }"

echo Fixing String.formatted() calls...
powershell -Command "Get-ChildItem -Path '%SRC_DIR%' -Filter '*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace '\.formatted\(', '); String.format(' | Set-Content $_.FullName }"
powershell -Command "Get-ChildItem -Path '%SRC_DIR%' -Filter '*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace '\"(.*)\"\.formatted\((.*)\);', 'String.format(\"$1\", $2);' | Set-Content $_.FullName }"

echo Fixing method references in toArray()...
powershell -Command "Get-ChildItem -Path '%SRC_DIR%' -Filter '*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace 'toArray\(([A-Za-z0-9_]*)\[\]::new\)', 'toArray(new $1[0])' | Set-Content $_.FullName }"

echo Fixing collection factory methods...
powershell -Command "Get-ChildItem -Path '%SRC_DIR%' -Filter '*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace 'List\.of\(', 'Arrays.asList(' | Set-Content $_.FullName }"
powershell -Command "Get-ChildItem -Path '%SRC_DIR%' -Filter '*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace 'List\.of\(\)', 'Collections.emptyList()' | Set-Content $_.FullName }"
powershell -Command "Get-ChildItem -Path '%SRC_DIR%' -Filter '*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace 'Set\.of\(\)', 'Collections.emptySet()' | Set-Content $_.FullName }"
powershell -Command "Get-ChildItem -Path '%SRC_DIR%' -Filter '*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace 'Map\.of\(\)', 'Collections.emptyMap()' | Set-Content $_.FullName }"

echo Fixing Stream.toList() calls...
powershell -Command "Get-ChildItem -Path '%SRC_DIR%' -Filter '*.java' -Recurse | ForEach-Object { (Get-Content $_.FullName) -replace '\.toList\(\)', '.collect(Collectors.toList())' | Set-Content $_.FullName }"

echo Identifying files with var keyword...
powershell -Command "$files = Get-ChildItem -Path '%SRC_DIR%' -Filter '*.java' -Recurse | Select-String -Pattern '\bvar\s+' | Group-Object Path | Select-Object -ExpandProperty Name; foreach ($file in $files) { Write-Host \"Warning: Found 'var' keyword in $file that needs manual fixing\" }"

echo Warning: Pattern matching with instanceof requires manual fixing
echo Warning: Switch expressions require manual fixing

echo.
echo Basic automatic fixes applied. Manual fixes may still be required.
echo See java8-compatibility-fixes.md for guidance on manual fixes.
pause 