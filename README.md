# Randoop Recreation: Feedback-Directed Test Generator

## Overview

This project is a simplified reimplementation of Randoop, a feedback-directed random test generation tool. It automatically generates unit tests for Java classes by building sequences of method calls and evaluating them through contracts and filters. The goal is to improve statement/branch coverage while reducing the manual effort needed for writing test suites.

## Features

- Feedback-directed sequence generation
- Structural equivalence filtering (avoid redundant test logic)
- Filters for exception, null return, and repeated output
- Weighted method selection to prioritize less-tested methods
- Support for constructor calls and primitive value reuse

## How It Works

1. A random class and method are selected from a provided Java class.
2. The tool attempts to build valid sequences of method calls with appropriate arguments.
3. It executes the sequences and evaluates them using various filters.
4. Valid and invalid sequences are output as JUnit test cases.

## Getting Started

### Prerequisites

- Java 21 (or update `pom.xml` to your JDK version)
- Maven (3.6+ recommended)

### Project Structure

```
src/
  main/java/com/woops/          <- Core engine files
  main/java/com/woops/filters/  <- Default filters
  test/java/com/demo/           <- Demo test class (TestClass.java)
```

### How to Run

1. Compile the project:

   ```bash
   mvn clean compile
   ```

2. Run the test generator with your compiled class:

   ```bash
   mvn exec:java -Dexec.mainClass="com.woops.Main" -Dexec.args="target/classes com.demo.TestClass"
   ```

3. Generated tests will be saved to:

   ```
   ./target/generated-sources/GeneratedTests.java
   ```

4. Run the generated tests:

   ```bash
   mvn test
   ```

## Customization

- Add your own filters in `com.woops.filters` and register them in `SequenceGenerator.java`
- Add your own contracts for runtime validation (in development)

## Example Output

Sample output of a generated test:

```java
@Test
public void generatedTest_123456789() throws Throwable {
  int result = com.demo.TestClass.add(3, 4);
  Assertions.assertEquals(7, result);
}
```

## Authors

- 
- Zhengying Sun zsa65 zsa65@sfu.ca
- 

## License

This project is for academic use in CMPT 479 and follows the course's academic integrity policy.

