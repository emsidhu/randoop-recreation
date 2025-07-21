package com.woops;

import com.woops.filters.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Generates sequences for test case generation using random method selection,
 * filtering, and structure-based equivalence checking.
 */
public class SequenceGenerator {

  public static Pair<List<Sequence>, List<Sequence>> generateSequences(List<Class<?>> classes, long timeLimit, int maxSequences) {
    List<Sequence> errorSeqs = new ArrayList<>();
    List<Sequence> validSeqs = new ArrayList<>();
    Set<String> seenFingerprints = new HashSet<>();

    long startTime = System.currentTimeMillis();
    int sequenceCount = 0;

    List<Filter> filters = List.of(
        new ExceptionFilter(),
        new NullFilter(),
        new EqualityFilter()
    );

    while (System.currentTimeMillis() - startTime < timeLimit &&
           sequenceCount < maxSequences) {

      // Pick random class and method
      Class<?> cls = classes.get(new Random().nextInt(classes.size()));
      Method[] methods = cls.getDeclaredMethods();
      if (methods.length == 0) continue;

      Method method = methods[new Random().nextInt(methods.length)];
      List<Sequence> seqs = new ArrayList<>();
      List<Object> args = new ArrayList<>();

      // Handle instance method: add receiver object
      if (!Modifier.isStatic(method.getModifiers())) {
        try {
          Constructor<?> constructor = cls.getDeclaredConstructor();
          Object receiver = constructor.newInstance();
          args.add(receiver);
        } catch (Exception e) {
          continue;
        }
      }

      // Generate arguments
      for (Class<?> type : method.getParameterTypes()) {
        args.add(getRandomValue(type));
      }

      // Create sequence and execute
      Sequence newSeq = Sequence.extend(method, seqs, args);
      try {
        newSeq.execute();

        // Check structural equivalence
        String fingerprint = newSeq.getSignatureFingerprint();
        if (seenFingerprints.contains(fingerprint)) {
          System.out.println("Sequence skipped due to duplicate structure");
          continue;
        }

        // Apply filters
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
          seenFingerprints.add(fingerprint);
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

  // Random argument generation
  private static Object getRandomValue(Class<?> type) {
    Random rand = new Random();
    if (type == int.class || type == Integer.class) return rand.nextInt(10);
    if (type == boolean.class || type == Boolean.class) return rand.nextBoolean();
    if (type == char.class || type == Character.class) return (char) ('a' + rand.nextInt(26));
    if (type == String.class) return "str" + rand.nextInt(100);
    return null;
  }
}
