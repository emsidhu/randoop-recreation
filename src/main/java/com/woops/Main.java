package com.woops;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class Main 
{
  public static void main( String[] args ) {
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
    // TODO: don't just print this, use their toCode method to output the sequences into a test suite
    System.out.println("Valid Sequences: ");
    for (Sequence seq : sequencePair.first) {
      System.out.println("Generated Sequence: ");
      System.out.println(seq.toCode());
    }
    System.out.println();
    System.out.println("Invalid (Error-Causing / Contract Violating) Sequences: ");
    for (Sequence seq : sequencePair.second) {
      System.out.println("Generated Sequence: ");
      System.out.println(seq.toCode());
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
