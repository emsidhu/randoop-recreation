package com.woops;

import com.woops.filters.*;

import java.lang.reflect.Array;
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
  public static Pair<List<Sequence>, List<Sequence>> generateSequences(
      List<Class<?>> classes, long timeLimit, int maxSequences, List<String> allowedMethods) {

    List<Sequence> errorSeqs = new ArrayList<>();
    SequencePool pool = new SequencePool();
    List<Sequence> validSeqs = new ArrayList<>();

    long startTime = System.currentTimeMillis();
    int sequenceCount = 0;

    List<Filter> filters = List.of(
        new ExceptionFilter(),
        new NullFilter(),
        new EqualityFilter()
    );

    // Get testable methods from each class
    List<List<Method>> classMethodLists = new ArrayList<>();
    for (Class<?> cls : classes) {
      Method[] allMethods = cls.getDeclaredMethods();
      List<Method> methods = new ArrayList<>();
      
      for (Method method : allMethods) {
        if (Modifier.isPublic(method.getModifiers()) && 
            shouldIncludeMethod(method, allowedMethods)) {
          methods.add(method);
        }
      }
      classMethodLists.add(methods);
    }

    while (System.currentTimeMillis() - startTime < timeLimit &&
           sequenceCount < maxSequences) {

      // Pick a random class and method
      int classIndex = new Random().nextInt(classes.size());
      Class<?> cls = classes.get(classIndex);
      List<Method> methods = classMethodLists.get(classIndex);

      if (methods.size() == 0) continue;

      Method method = methods.get(new Random().nextInt(methods.size()));

      Sequence newSeq = new Sequence();
      
      // 80% chance to start from an existing valid sequence, 20% chance to start empty
      if (!validSeqs.isEmpty() && random.nextDouble() < 0.8) {
        // Use an existing valid sequence as starting point
        Sequence baseSeq = validSeqs.get(random.nextInt(validSeqs.size()));
        newSeq.concat(baseSeq);
      }

      List<Argument> args = new ArrayList<>();

      // Make a receiver the first argument for non-static methods
      if (!Modifier.isStatic(method.getModifiers())) {
        // First check current sequence
        Statement receiverStmt = pool.findStatementOfType(newSeq, cls);
        if (receiverStmt == null) {
          // Check other sequences
          Sequence receiverSequence = pool.findSequenceOfType(cls);
          if (receiverSequence != null) {
            newSeq.concat(receiverSequence);
            // Get the required value out of the sequence
            receiverStmt = pool.findStatementOfType(receiverSequence, cls);
            args.add(new Argument(receiverStmt));
          }
        } else {
          args.add(new Argument(receiverStmt));
        }

        // If no receiver exists, create one
        if (receiverStmt == null) { 
          try { 
            // TODO: Make this work for constructors that need arguments as well
            Constructor<?> constructor = cls.getDeclaredConstructor();
            Statement constructorStmt = new ConstructorCall(constructor, new ArrayList<>());
            newSeq.statements.add(constructorStmt);
            
            constructorStmt.execute();
            args.add(new Argument(constructorStmt));
          } catch (Exception e) {
            System.err.println("CONTRACT FAIL " + e.getMessage());
            continue;
          }
        }
      }

      for (Class<?> type : method.getParameterTypes()) {
        // 20% chance to use a random value regardless of usable statements
        if (random.nextDouble() < 0.2) {
          Object randomValue = getRandomValue(type);
          Statement constantStmt = new ConstantAssignment(randomValue, type);
          newSeq.statements.add(constantStmt);
          args.add(new Argument(constantStmt));
          continue;
        }

        // Check if current sequence contains usable statement
        Statement argStmt = pool.findStatementOfType(newSeq, type);
        if (argStmt != null) {
          args.add(new Argument(argStmt));
        } else {
          // Otherwise, check pool
          Sequence argSequence = pool.findSequenceOfType(type);
          if (argSequence != null) {
            newSeq.concat(argSequence);
            argStmt = pool.findStatementOfType(argSequence, type);
            args.add(new Argument(argStmt));
          } else {
            Object randomValue = getRandomValue(type);
            Statement constantStmt = new ConstantAssignment(randomValue, type);
            newSeq.statements.add(constantStmt);
            args.add(new Argument(constantStmt));
          }
        }
      }
      
      newSeq.statements.add(new MethodCall(method, args));
      sequenceCount++;

      try {
        // execute sequence
        newSeq.execute();
      } catch (Exception e) {
        System.out.println("Exception during execution: " + e);
        errorSeqs.add(newSeq);
      }
      
      boolean passedAll = true;
      // Apply filters
      for (Filter f : filters) {
        if (!f.isValid(newSeq)) {
          System.out.println("Sequence filtered by " + f.getName());
          passedAll = false;
          break;
        }
      }

      // Check for contract violations
      String violatedContract = null;
      for (Statement stmt : newSeq.statements) {
        if (stmt.getResult() != null) {
          String contractViolation = ContractChecker.checkAll(stmt.getResult());
          if (contractViolation != null) {
            System.out.println("Sequence violates contract: " + violatedContract);
            newSeq.setViolatedContract(violatedContract);
            passedAll = false;
            break;
          }
        }
      }

      if (passedAll) {
        System.out.println("Sequence accepted:");
        System.out.println(newSeq.toCode(true));
        validSeqs.add(newSeq);
      } else {
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

    // Allows for Array generation
    if (type.isArray()) {
      Class<?> componentType = type.getComponentType();
      int length = random.nextInt(20);
      Object array = Array.newInstance(componentType, length);
      for (int i = 0; i < length; i++) {
          Array.set(array, i, getRandomValue(componentType));
      }
      return array;
    }

    // Allows for List generation
    if (type == List.class) {
      List<Object> list = new ArrayList<>();
      int size = random.nextInt(20);
      for (int i = 0; i < size; i++) {
          list.add(getRandomValue(Object.class));
      }
      return list;
    } 

    return null; // for other object types
  }

  // Helper method to check if method should be included
  private static boolean shouldIncludeMethod(Method method, List<String> allowedMethods) {
    // If no methods specified, allow all methods
    if (allowedMethods.isEmpty()) return true;
    // Check if method name is in the allowed list
    return allowedMethods.contains(method.getName());
  }

}