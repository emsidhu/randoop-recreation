package com.woops;

import java.io.File;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.io.IOException;        // add this import
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.MalformedURLException;
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
    int maxSequences = 50;       // Default number of sequences to generate
    double reuseProb = 0.85;     // Default reuse probability

  
    // Parse command-line arguments
    List<String> methodNames = new ArrayList<>();
    for (String arg : args) {
      if (arg.startsWith("--dir=")) {
        dirArg = arg.substring("--dir=".length());
      } else if (arg.startsWith("--class=")) {
        classArg = arg.substring("--class=".length());
      } else if (arg.startsWith("--time=")) {
        timeLimit = Integer.parseInt(arg.substring("--time=".length()));
      } else if (arg.startsWith("--max=")) {
        maxSequences = Integer.parseInt(arg.substring("--max=".length()));
      } else if (arg.startsWith("--reuse-prob=")) {
        reuseProb = Double.parseDouble(arg.substring("--reuse-prob=".length()));
      } else {
        methodNames.add(arg);
      }
    }
  
    // Validate required arguments
    if (dirArg == null || classArg == null) {
      System.err.println("Usage: mvn exec:java -Dexec.args=\"--dir=<class-dir> --class=com.<package>.<class-name> --time=<max-seconds> --reuse-prob=<probability> [method1 method2 ...]\"");
      System.err.println("If no methods specified, all public methods will be used");
      return;
    }

    if (reuseProb > 1 || reuseProb < 0) {
      System.err.println("Reuse probability must be within 0-1");
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
      for (URLClassLoader l : openLoaders) { l.close(); }
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
        SequenceGenerator.generateSequences(classes, timeLimit, maxSequences, methodNames, reuseProb);
  
    // Generate JUnit test class
    String suiteClassName = "GeneratedTests";
    String outDir = "./target/generated-sources/com/demo";

    try {
      Path dirPath = Paths.get(outDir);
      Files.createDirectories(dirPath);
      Path filePath = dirPath.resolve(suiteClassName + ".java");
  
      try (BufferedWriter w = Files.newBufferedWriter(filePath)) {
        w.write("""
                package com.demo;
                import org.junit.jupiter.api.Assertions;
                import org.junit.jupiter.api.Test;
  
                public class %s {
                """.formatted(suiteClassName));
  
        System.out.println("Valid Sequences:");
        for (Sequence seq : sequencePair.first) {
          w.write(seq.toCode(true));
          w.newLine();
        }
  
        System.out.println();
        System.out.println("Invalid Sequences:");
        for (Sequence seq : sequencePair.second) {
          if (seq.getThrewException() == true) {
            w.write(seq.toCode(false));
            w.newLine();
          }
        }
  
        // Close the test class
        w.write("}");
      }
  
      System.out.printf("Wrote sequences to %s%n",filePath);
  
    } catch (IOException ioe) {
      System.err.println("Failed to write test class: " + ioe.getMessage());
      ioe.printStackTrace();
    }
  }
  

  private static final List<URLClassLoader> openLoaders = new ArrayList<>();
  // Returns a Class 
  private static Class<?> getClassFromFile(File dir, String className)  throws MalformedURLException, ClassNotFoundException {
    URL url = dir.toPath().toAbsolutePath().toUri().toURL();
    if (!url.toString().endsWith("/")) {          // ensure it’s treated as a dir
        url = new URL(url.toString() + "/");
    }
    URLClassLoader loader =
        new URLClassLoader(new URL[] { url }, Main.class.getClassLoader());
    openLoaders.add(loader);                      // remember to close later
    return Class.forName(className, false, loader);
  }




}