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
  public static void main(String[] args) throws Exception  {
    // TODO: Add checks to ensure arguments are correctly passed
    // TODO: Allow arguments to be passed in using flags
    // TODO: Allow people to pass in multiple class names

    if (args.length < 2) {
      System.err.println("Error: Too few arguments, need class directory and name");
      return;
    }
    File classDir = new File(args[0]);
    // The class name should be the fully qualified name, e.g., "com.woops.Class"
    String className = args[1];
    int timeLimit = 1000;
    int maxSequences = 5;
    
    if (!classDir.exists() || !classDir.isDirectory()) {
      System.err.println("Error: The provided path does not point to a valid directory.");
      return;
    }


    List<Class<?>> classes = new ArrayList<>();
    // Get the class object
    Class<?> cls = getClassFromFile(classDir, className);
    if (cls == null) return;
    classes.add(cls);

    Pair<List<Sequence>,List<Sequence>> sequencePair = SequenceGenerator.generateSequences(classes, timeLimit, maxSequences);

    // Generates the test suite
    String suiteClassName = "GeneratedTests";
    String outDir  = "./target/generated-sources";
    try {
      
      Path   dirPath = Paths.get(outDir);
      Files.createDirectories(dirPath);
      Path   filePath = dirPath.resolve(suiteClassName + ".java");

      try (BufferedWriter w = Files.newBufferedWriter(filePath)) {

        w.write("""
                package com.demo;
                import org.junit.jupiter.api.Assertions;
                import org.junit.jupiter.api.Test;

                public class %s {
                """.formatted(suiteClassName));

        System.out.println("Valid Sequences: ");
        for (Sequence seq : sequencePair.first) {
          w.write(seq.toCode());
          w.newLine();
        }

        System.out.println();
        System.out.println("Invalid (Error-Causing / Contract Violating) Sequences: ");
        for (Sequence seq : sequencePair.second) {
          w.write(seq.toCode());
          w.newLine();
        }

        // close class
        w.write("}");
      }

      System.out.printf("Wrote %d valid and %d invalid sequences to %s%n",
          sequencePair.first.size(), sequencePair.second.size(), filePath);

    } catch (IOException ioe) {
      System.err.println("Failed to write generated tests: " + ioe.getMessage());
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
