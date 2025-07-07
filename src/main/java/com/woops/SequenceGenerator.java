package com.woops;

import com.woops.filters.*; 

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
    * @return A Pair containing a list of the valid, and error-causing, Sequences
  */
  public static Pair<List<Sequence>, List<Sequence>> generateSequences(List<Class<?>> classes, long timeLimit, int maxSequences) {
    List<Sequence> errorSeqs = new ArrayList<>();
    List<Sequence> validSeqs = new ArrayList<>();
    long startTime = System.currentTimeMillis();
    int sequenceCount = 0;

    // ✅ 初始化默认 filters
    List<Filter> filters = List.of(
        new ExceptionFilter(),
        new NullFilter(),
        new EqualityFilter()
    );

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
          Constructor<?> constructor = cls.getDeclaredConstructor();
          Object receiver = constructor.newInstance(); // Class whatever = new Class();
          args.add(receiver);
        } catch (Exception e) {
          System.err.println("CONTRACT FAIL " + e.getMessage());
          continue;
        }
      }

      for (Class<?> type : method.getParameterTypes()) {
        args.add(getRandomValue(type));
      }

      Sequence newSeq = Sequence.extend(method, seqs, args);

      try {
        // execute sequence
        newSeq.execute();

        // apply filters to do the judgement
        boolean passedAll = true;
        for (Filter f : filters) {
          if (!f.isValid(newSeq)) {
            System.out.println("Sequence filtered by " + f.getName());
            passedAll = false;
            break;
          }
        }

        if (passedAll) {
          System.out.println("Sequence accepted:");
          System.out.println(newSeq.toCode());
          validSeqs.add(newSeq);
        } else {
          errorSeqs.add(newSeq);
        }

        sequenceCount++;

      } catch (Exception e) {
        System.out.println("Exception during execution: " + e);
        errorSeqs.add(newSeq);
      }
    }

    return new Pair<>(validSeqs, errorSeqs);
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
