package com.woops;

import java.io.File;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.io.IOException;        // add this import
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class Main 
{
  public static void main(String[] args) throws Exception {
    String dirArg = null;
    String classArg = null;
    int timeLimit = 1000;       // Default timeout in milliseconds
    int maxSequences = 5;       // Default number of sequences to generate
  
    // Parse command-line arguments
    for (String arg : args) {
      if (arg.startsWith("--dir=")) {
        dirArg = arg.substring("--dir=".length());
      } else if (arg.startsWith("--class=")) {
        classArg = arg.substring("--class=".length());
      } else if (arg.startsWith("--time=")) {
        timeLimit = Integer.parseInt(arg.substring("--time=".length()));
      } else if (arg.startsWith("--max=")) {
        maxSequences = Integer.parseInt(arg.substring("--max=".length()));
      }
    }
  
    // Validate required arguments
    if (dirArg == null || classArg == null) {
      System.err.println("Usage: mvn exec:java -Dexec.args=\"--dir=target/classes --class=com.demo.TestClass\"");
      return;
    }
  
    File classDir = new File(dirArg);
    if (!classDir.exists() || !classDir.isDirectory()) {
      System.err.println("Error: The provided directory is invalid.");
      return;
    }
  
    // Load all specified class names (comma-separated)
    String[] classNames = classArg.split(",");
    List<Class<?>> classes = new ArrayList<>();
  
    for (String className : classNames) {
      if (!className.matches("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[A-Z][a-zA-Z\\d_$]*")) {
        System.err.println("Error: Invalid class name format: " + className);
        return;
      }
  
      Class<?> cls = getClassFromFile(classDir, className.trim());
      if (cls != null) {
        classes.add(cls);
      } else {
        System.err.println("Warning: Failed to load class " + className);
      }
    }
  
    if (classes.isEmpty()) {
      System.err.println("Error: No valid class loaded.");
      return;
    }
  
    // Run sequence generation
    Pair<List<Sequence>, List<Sequence>> sequencePair =
        SequenceGenerator.generateSequences(classes, timeLimit, maxSequences);
  
    // Generate JUnit test class
    String suiteClassName = "GeneratedTests";
    String outDir = "./target/generated-sources";
  
    try {
      Path dirPath = Paths.get(outDir);
      Files.createDirectories(dirPath);
      Path filePath = dirPath.resolve(suiteClassName + ".java");
  
      try (BufferedWriter w = Files.newBufferedWriter(filePath)) {
        w.write("""
                import org.junit.jupiter.api.Assertions;
                import org.junit.jupiter.api.Test;
  
                public class %s {
                """.formatted(suiteClassName));
  
        System.out.println("Valid Sequences:");
        for (Sequence seq : sequencePair.first) {
          w.write(seq.toCode());
          w.newLine();
        }
  
        System.out.println();
        System.out.println("Invalid Sequences:");
        for (Sequence seq : sequencePair.second) {
          String testName = "generatedInvalidTest_" + Math.abs(seq.hashCode());
          w.write("""
                    @Test
                    public void %s() {
                      Assertions.assertThrows(Throwable.class, () -> {
              """.formatted(testName));
  
          for (String line : seq.toCode().split("\\R")) {
            w.write("        " + line);
            w.newLine();
          }
  
          w.write("""
                      });
                    }
                  """);
          w.newLine();
        }
  
        // Close the test class
        w.write("}");
      }
  
      System.out.printf("Wrote %d valid and %d invalid sequences to %s%n",
          sequencePair.first.size(), sequencePair.second.size(), filePath);
  
    } catch (IOException ioe) {
      System.err.println("Failed to write test class: " + ioe.getMessage());
      ioe.printStackTrace();
    }
  }
  

  // Returns a Class 
  private static Class<?> getClassFromFile(File dir, String className) {
    try (URLClassLoader classLoader = new URLClassLoader(new URL[] {dir.toURI().toURL()})) {
      return classLoader.loadClass(className);
    } catch (Exception e) {
      System.err.println("Error loading class: " + e.getMessage());
      return null;
    }
  }
}
