package com.woops;

import java.lang.reflect.Method;
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
    * @return List of successfully executed sequences.
  */
  public static List<Sequence> generateSequences(List<Class<?>> classes, long timeLimit, int maxSequences) {
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

      Method m = methods[new Random().nextInt(methods.length)];
      
      

      

      // Generate arguments for the chosen method
      List<Object> args = new ArrayList<>();
      for (Class<?> type : m.getParameterTypes()) {
        args.add(getRandomValue(type));
      }


      Sequence newSeq = Sequence.extend(m, nonErrorSeqs, args);
      try {
        // TODO: Execute the sequence and check for errors
          // newSeq.execute();
        nonErrorSeqs.add(newSeq);
        sequenceCount++;
      } catch (Exception e) {
        // If an error occurs, add to errorSeqs
        errorSeqs.add(newSeq);
      }
    }

    return nonErrorSeqs;
  }

  // Returns a random value for the given type.
  private static Object getRandomValue(Class<?> type) {
    Random rand = new Random();
    if (type == int.class || type == Integer.class) return rand.nextInt(10);
    if (type == boolean.class || type == Boolean.class) return rand.nextBoolean();
    if (type == char.class || type == Character.class) return (char) ('a' + rand.nextInt(26));
    if (type == String.class) return "str" + rand.nextInt(100);
    return null; // for other object types
  }
}
