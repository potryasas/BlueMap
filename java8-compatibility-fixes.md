# Java 8 Compatibility Fixes for BlueMap

This document outlines the common Java 8 compatibility issues encountered when compiling BlueMap, and how to fix them.

## 1. Method Reference in `toArray()`

**Issue**: Java 8 doesn't support method references in `toArray()` calls.

**Example of incompatible code**:
```java
list.toArray(String[]::new);
```

**Fix**:
```java
list.toArray(new String[list.size()]);
```

## 2. `String.formatted()` Method (Java 15+)

**Issue**: `String.formatted()` was added in Java 15.

**Example of incompatible code**:
```java
String formatted = "Hello %s".formatted(name);
```

**Fix**:
```java
String formatted = String.format("Hello %s", name);
```

## 3. `Path.of()` Method (Java 11+)

**Issue**: `Path.of()` was added in Java 11.

**Example of incompatible code**:
```java
Path path = Path.of("directory", "file.txt");
```

**Fix**:
```java
// Option 1: Using Paths.get()
Path path = Paths.get("directory", "file.txt");

// Option 2: Using PathUtil.path() if available
Path path = PathUtil.path("directory", "file.txt");
```

## 4. Pattern Matching with `instanceof` (Java 16+)

**Issue**: Pattern matching with `instanceof` was introduced in Java 16.

**Example of incompatible code**:
```java
if (obj instanceof String s) {
    // Use s directly
    System.out.println(s.length());
}
```

**Fix**:
```java
if (obj instanceof String) {
    String s = (String) obj;
    // Now use s
    System.out.println(s.length());
}
```

## 5. Switch Expressions (Java 14+)

**Issue**: Switch expressions were introduced in Java 14.

**Example of incompatible code**:
```java
int result = switch(day) {
    case MONDAY, FRIDAY -> 1;
    case TUESDAY -> 2;
    default -> 0;
};
```

**Fix**:
```java
int result;
switch(day) {
    case MONDAY: 
    case FRIDAY: 
        result = 1;
        break;
    case TUESDAY: 
        result = 2;
        break;
    default: 
        result = 0;
}
```

## 6. List.of(), Set.of(), Map.of() (Java 9+)

**Issue**: Collection factory methods were added in Java 9.

**Example of incompatible code**:
```java
List<String> list = List.of("a", "b", "c");
Set<String> set = Set.of("a", "b", "c");
Map<String, Integer> map = Map.of("a", 1, "b", 2);
```

**Fix**:
```java
// For List
List<String> list = Arrays.asList("a", "b", "c");
// Or for an empty list
List<String> emptyList = Collections.emptyList();

// For Set
Set<String> set = new HashSet<>(Arrays.asList("a", "b", "c"));
// Or for an empty set
Set<String> emptySet = Collections.emptySet();

// For Map
Map<String, Integer> map = new HashMap<>();
map.put("a", 1);
map.put("b", 2);
// Or for an empty map
Map<String, Integer> emptyMap = Collections.emptyMap();
```

## 7. Stream API `toList()` (Java 16+)

**Issue**: `Stream.toList()` method was added in Java 16.

**Example of incompatible code**:
```java
List<String> result = stream.filter(s -> s.length() > 3).toList();
```

**Fix**:
```java
List<String> result = stream.filter(s -> s.length() > 3).collect(Collectors.toList());
```

## 8. Diamond Operator with Anonymous Inner Classes (Java 9+)

**Issue**: Diamond operator with anonymous inner classes was improved in Java 9.

**Example of incompatible code**:
```java
Map<String, List<Integer>> map = new HashMap<>() {
    {
        put("a", Arrays.asList(1, 2, 3));
    }
};
```

**Fix**:
```java
Map<String, List<Integer>> map = new HashMap<String, List<Integer>>() {
    {
        put("a", Arrays.asList(1, 2, 3));
    }
};
```

## 9. `var` Keyword (Java 10+)

**Issue**: The `var` keyword for local variable type inference was introduced in Java 10.

**Example of incompatible code**:
```java
var list = new ArrayList<String>();
var name = "John";
```

**Fix**:
```java
ArrayList<String> list = new ArrayList<>();
String name = "John";
```

## 10. InputStream Methods (`readAllBytes`, `readNBytes`, `transferTo`) (Java 9+)

**Issue**: These methods were added to `InputStream` in Java 9.

**Example of incompatible code**:
```java
byte[] data = inputStream.readAllBytes();
```

**Fix**:
```java
// Using utility methods or Apache Commons IO
ByteArrayOutputStream buffer = new ByteArrayOutputStream();
int nRead;
byte[] data = new byte[16384];
while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
    buffer.write(data, 0, nRead);
}
byte[] result = buffer.toByteArray();
```

## 11. Java File to Path Conversion Issues

**Issue**: Type mismatches between `File` and `Path` objects.

**Example of incompatible code**:
```java
Path path = file; // Trying to directly assign a File to a Path
```

**Fix**:
```java
Path path = file.toPath(); // Convert File to Path properly
```

## 12. Type Inference Issues in Generic Methods

**Issue**: Java 8 type inference is less powerful than in later versions.

**Fix**: Explicitly provide type parameters to help the compiler:
```java
// Instead of:
method(arg);

// Use:
method.<String>method(arg);
```

## 13. Unhandled Exceptions

**Issue**: Unchecked exceptions being propagated without being caught or declared.

**Fix**: Add appropriate exception handling:
```java
try {
    // Code that can throw IOException
} catch (IOException e) {
    // Handle exception
    throw new RuntimeException("Error processing file", e);
}
```

## 14. Missing Constructors/Methods

**Issue**: Some classes might be missing no-arg constructors or other methods in Java 8.

**Fix**: Provide the required constructors/methods or use alternative approaches:
```java
// Add missing constructors
public MyClass() {
    this(DEFAULT_VALUE);
}

// Provide default implementations for abstract methods
@Override
public void requiredMethod() {
    // Default implementation
}
``` 