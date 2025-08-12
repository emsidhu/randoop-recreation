# Randoop Recreation: Feedback-Directed Test Generator

## Overview

This project is a simplified reimplementation of Randoop, a feedback-directed random test generation tool. It automatically generates unit tests for Java classes by building sequences of method calls and evaluating them through contracts and filters. The goal is to improve statement/branch coverage while reducing the manual effort needed for writing test suites.

## How It Works

1. A random class and method are selected from a provided Java class.
2. The tool attempts to build valid sequences of method calls with appropriate arguments.
3. It executes the sequences and evaluates them using various contracts/filters.
4. Valid and invalid sequences are output as JUnit test cases.

## Getting Started

### Prerequisites

- Java 21 (or update `pom.xml` to your JDK version)
- Maven (3.6+ recommended)

### How to Run

1. Compile the project:

   ```bash
   mvn clean compile
   ```

2. Run the test generator with your compiled class:

   ```bash
   mvn -e exec:java -Dexec.mainClass="com.woops.Main" -Dexec.args="--dir=./target/classes --class=com.demo.FibHeap --reuse-prob=0.85"
   ```

3. Generated tests are saved to:

   ```
   ./target/generated-sources/GeneratedTests.java
   ```

4. Run the generated tests:

   ```bash
   mvn test
   ```
#### Run with selected methods:

If you want to generate tests using specific methods, use:

```bash
mvn -e exec:java -Dexec.mainClass="com.woops.Main" -Dexec.args="--dir=./target/classes --class=com.demo.BinTree find add remove"
```

#### Run with multiple classes:

If you want to test with multiple classes, save them to a single directory and list them comma-separated:

```bash
mvn -e exec:java -Dexec.mainClass="com.woops.Main" -Dexec.args="--dir=./target/classes --class=com.demo.BinTree,com.demo.FibHeap"
```

## Example Output

Sample output of a generated test:

```java
@Test
public void validGeneratedTest_1821264475() throws Throwable {
  com.demo.FibHeap var0 = new com.demo.FibHeap();
  int var1 = -40;
  var0.insert(var1);
  Assertions.assertEquals(var0, var0);
  Assertions.assertDoesNotThrow(() -> var0.hashCode());
  Assertions.assertDoesNotThrow(() -> var0.toString());
}
```

## Authors

- Ekam Sidhu ess9 ess9@sfu.ca
- Zhengying Sun zsa65 zsa65@sfu.ca
- Alex Bruma aba215 aba215@sfu.ca

## License

This project is for academic use in CMPT 479 and follows the course's academic integrity policy.

