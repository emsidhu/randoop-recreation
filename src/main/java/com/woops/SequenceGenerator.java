package com.woops;

import com.woops.filters.*; 

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SequenceGenerator {
  private final static Random random = new Random();
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
    SequencePool pool = new SequencePool();
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

      Sequence newSeq = new Sequence();

      List<Object> args = new ArrayList<>();

      // Make a receiver the first argument for non-static methods
      if (!Modifier.isStatic(method.getModifiers())) {
        Sequence receiverSequence = pool.findSequenceOfType(cls);
        if (receiverSequence != null) {
          newSeq.concat(receiverSequence);
          // Get the required value out of the sequence
          Statement receiverStmt = pool.findStatementOfType(receiverSequence, cls);
          args.add(receiverStmt.getResult());
        } else { 
          try { 
            // TODO: Make this work for constructors that need arguments as well
            Constructor<?> constructor = cls.getDeclaredConstructor();
            Statement constructorStmt = new ConstructorCall(constructor, new ArrayList<>());
            newSeq.statements.add(constructorStmt);
            
            constructorStmt.execute();
            args.add(constructorStmt.getResult());
          } catch (Exception e) {
            System.err.println("CONTRACT FAIL " + e.getMessage());
            continue;
          }
        }
      }

      for (Class<?> type : method.getParameterTypes()) {
        // 50% chance to use a random value regardless of usable statements
        if (random.nextBoolean()) {
          args.add(getRandomValue(type));
          continue;
        }

        // Check if current sequence contains usable statement
        Statement argStmt = pool.findStatementOfType(newSeq, type);
        if (argStmt != null) {
          args.add(argStmt.getResult());
        } else {
          // Otherwise, check pool
          Sequence argSequence = pool.findSequenceOfType(type);
          if (argSequence != null) {
            newSeq.concat(argSequence);
            argStmt = pool.findStatementOfType(argSequence, type);
            args.add(argStmt.getResult());
          } else {
            args.add(getRandomValue(type));
          }
        }
      }

      newSeq.statements.add(new MethodCall(method, args));

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
          pool.addSequence(newSeq);
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

  // Returns a random string
  private static String generateRandomString(int length) {
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < length; i++) {
      int index = random.nextInt(characters.length());
      sb.append(characters.charAt(index));
    }
    return sb.toString();
  }

  // Returns a random value for the given type.
  private static Object getRandomValue(Class<?> type) {
    if (type == int.class || type == Integer.class) return random.nextInt(100) * (int) Math.signum(random.nextInt());
    if (type == boolean.class || type == Boolean.class) return random.nextBoolean();
    if (type == char.class || type == Character.class) return (char) (32 + random.nextInt(95));
    if (type == String.class) return generateRandomString(random.nextInt(50)); 
    return null; // for other object types
  }

}