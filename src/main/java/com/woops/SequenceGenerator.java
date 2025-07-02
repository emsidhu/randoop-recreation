package com.woops;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SequenceGenerator {
  /** 
    * Generates a list of sequences from the given classes.
    *
    * @param classes List of classes to generate sequences from.
    * @param timeLimit Time limit in milliseconds.
    * @param maxSequences Maximum number of sequences to generate.
    * @return A Pair containing a list of the valid, and error causing, Sequences
  */
  public static Pair<List<Sequence>,List<Sequence>> generateSequences(List<Class<?>> classes, long timeLimit, int maxSequences) {
    List<Sequence> errorSeqs = new ArrayList<>();
    List<Sequence> nonErrorSeqs = new ArrayList<>();
    long startTime = System.currentTimeMillis();
    int sequenceCount = 0;

    while (System.currentTimeMillis() - startTime < timeLimit && 
        sequenceCount < maxSequences) {

      // Pick a random class and method
      Class<?> cls = classes.get(new Random().nextInt(classes.size()));
      Method[] methods = cls.getDeclaredMethods();
      if (methods.length == 0) continue; 

      Method method = methods[new Random().nextInt(methods.length)];
      
      List<Sequence> seqs = new ArrayList<>();
      List<Object> args = new ArrayList<>();

      // Make a receiver the first argument for non-static methods
      if (!Modifier.isStatic(method.getModifiers())) {
        try { 
          // TODO: Make this work even when there is no default constructor
          Constructor<?> constructor = cls.getDeclaredConstructor();
          Object receiver = constructor.newInstance(); // Class whatever = new Class();
          args.add(receiver);
        } catch (Exception e) {
          continue;
        }
      }
      
      
      // TODO: Allow use of methodCall return values in newSeq (if return value is needed) and 
        // pass in utilized sequences to extend 
      for (Class<?> type : method.getParameterTypes()) {
        args.add(getRandomValue(type));
      }
      Sequence newSeq = Sequence.extend(method, seqs, args);

      try {
        newSeq.execute();
        nonErrorSeqs.add(newSeq);
        sequenceCount++;
      } catch (Exception e) {
        // If an error occurs, add to errorSeqs
        System.out.println("Error: " + e);
        errorSeqs.add(newSeq);
      }
    }

    return new Pair<>(nonErrorSeqs, errorSeqs);
  }

  // Returns a random value for the given type.
  private static Object getRandomValue(Class<?> type) {
    Random rand = new Random();
    if (type == int.class || type == Integer.class) return rand.nextInt(10);
    if (type == boolean.class || type == Boolean.class) return rand.nextBoolean();
    if (type == char.class || type == Character.class) return (char) ('a' + rand.nextInt(26));
    if (type == String.class) return "\"str" + rand.nextInt(100) + "\""; 
    return null; // for other object types
  }
}
