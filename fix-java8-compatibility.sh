#!/bin/bash
# Script to automatically fix common Java 8 compatibility issues

echo "BlueMap Java 8 Compatibility Fixer"
echo "=================================="
echo 

SRC_DIR="."
if [ ! -z "$1" ]; then
    SRC_DIR="$1"
fi

echo "Scanning source directory: $SRC_DIR"
echo

# 1. Fix Path.of() method calls
echo "Fixing Path.of() calls..."
find "$SRC_DIR" -type f -name "*.java" -exec sed -i 's/Path\.of(/Paths.get(/g' {} \;
find "$SRC_DIR" -type f -name "*.java" -not -exec grep -l "import java.nio.file.Paths;" {} \; -exec grep -l "Path\.get" {} \; -exec sed -i '1,/^import/s/^import/import java.nio.file.Paths;\nimport/' {} \;

# 2. Fix String.formatted() method calls
echo "Fixing String.formatted() calls..."
find "$SRC_DIR" -type f -name "*.java" -exec sed -i 's/\.formatted(/); String.format(/g' {} \;
find "$SRC_DIR" -type f -name "*.java" -exec sed -i 's/"\(.*\)"\.formatted(\(.*\));/String.format("\1", \2);/g' {} \;

# 3. Fix method references in toArray()
echo "Fixing method references in toArray()..."
find "$SRC_DIR" -type f -name "*.java" -exec sed -i 's/toArray(\([A-Za-z0-9_]*\)\[\]::new)/toArray(new \1[0])/g' {} \;

# 4. Fix List.of(), Set.of(), Map.of()
echo "Fixing collection factory methods..."
find "$SRC_DIR" -type f -name "*.java" -exec sed -i 's/List\.of(/Arrays.asList(/g' {} \;
find "$SRC_DIR" -type f -name "*.java" -not -exec grep -l "import java.util.Arrays;" {} \; -exec grep -l "Arrays\.asList" {} \; -exec sed -i '1,/^import/s/^import/import java.util.Arrays;\nimport/' {} \;

find "$SRC_DIR" -type f -name "*.java" -exec sed -i 's/List\.of()/Collections.emptyList()/g' {} \;
find "$SRC_DIR" -type f -name "*.java" -exec sed -i 's/Set\.of()/Collections.emptySet()/g' {} \;
find "$SRC_DIR" -type f -name "*.java" -exec sed -i 's/Map\.of()/Collections.emptyMap()/g' {} \;
find "$SRC_DIR" -type f -name "*.java" -not -exec grep -l "import java.util.Collections;" {} \; -exec grep -l "Collections\.empty" {} \; -exec sed -i '1,/^import/s/^import/import java.util.Collections;\nimport/' {} \;

# 5. Fix stream toList() method
echo "Fixing Stream.toList() calls..."
find "$SRC_DIR" -type f -name "*.java" -exec sed -i 's/\.toList()/\.collect(Collectors.toList())/g' {} \;
find "$SRC_DIR" -type f -name "*.java" -not -exec grep -l "import java.util.stream.Collectors;" {} \; -exec grep -l "Collectors\.toList" {} \; -exec sed -i '1,/^import/s/^import/import java.util.stream.Collectors;\nimport/' {} \;

# 6. Fix var keyword
echo "Fixing var keywords..."
find "$SRC_DIR" -type f -name "*.java" -exec grep -l "var " {} \; > var_files.txt
while IFS= read -r file; do
    echo "Warning: Found 'var' keyword in $file that needs manual fixing"
done < var_files.txt
rm var_files.txt

# 7. Fix pattern matching with instanceof
echo "Warning: Pattern matching with instanceof requires manual fixing"

# 8. Fix switch expressions
echo "Warning: Switch expressions require manual fixing"

echo
echo "Basic automatic fixes applied. Manual fixes may still be required."
echo "See java8-compatibility-fixes.md for guidance on manual fixes." 