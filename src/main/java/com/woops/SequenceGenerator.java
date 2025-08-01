package com.woops;

import com.woops.filters.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Generates sequences for test case generation using random method selection,
 * filtering, and structure-based equivalence checking.
 */
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
    Set<String> seenFingerprints = new HashSet<>();

    long startTime = System.currentTimeMillis();
    int sequenceCount = 0;

    List<Filter> filters = FilterLoader.loadFiltersFromConfig("config.json");

    // Get testable methods from each class
    List<List<Method>> classMethodLists = new ArrayList<>();
    List<List<Constructor<?>>> classConstructorLists = new ArrayList<>();
    Map<Method, Integer> methodUsageCount = new HashMap<>();

    for (Class<?> cls : classes) {
      Method[] allMethods = cls.getDeclaredMethods();
      List<Method> methods = new ArrayList<>();
      
      // Get method list
      for (Method method : allMethods) {
        if (Modifier.isPublic(method.getModifiers()) && 
            shouldIncludeMethod(method, allowedMethods)) {
          methods.add(method);
          methodUsageCount.put(method, 0); // Initialize usage count
        }
      }
      classMethodLists.add(methods);

      // Get constructor list
      Constructor<?>[] constructors = cls.getDeclaredConstructors();
      List<Constructor<?>> publicConstructors = new ArrayList<>();
      
      for (Constructor<?> constructor : constructors) {
        if (Modifier.isPublic(constructor.getModifiers())) {
          publicConstructors.add(constructor);
        }
      }
      classConstructorLists.add(publicConstructors);
    }


    while (System.currentTimeMillis() - startTime < timeLimit &&
           sequenceCount < maxSequences) {


      // Pick a random class and method
      int classIndex = new Random().nextInt(classes.size());
      Class<?> cls = classes.get(classIndex);
      List<Method> methods = classMethodLists.get(classIndex);
      List<Constructor<?>> constructors = classConstructorLists.get(classIndex);

      if (methods.size() == 0) continue;
      Sequence newSeq = new Sequence();
      // 75% chance to start from an existing valid sequence, 25% chance to start empty
      if (!validSeqs.isEmpty() && random.nextDouble() < 0.75) {
        // Use an existing valid sequence as starting point
        Sequence baseSeq = validSeqs.get(random.nextInt(validSeqs.size()));
        newSeq.concat(baseSeq);
      }
      
      Method method = getRandomMethod(methods, methodUsageCount);      
      // Increment usage count for the chosen method
      methodUsageCount.put(method, methodUsageCount.get(method) + 1);

      // Add same method until repeatMethod is false 
      boolean repeatMethod = true;
      while (repeatMethod) {
        // 95% chance to use method again
        if (random.nextDouble() < 0.95) repeatMethod = false;
              List<Argument> args = new ArrayList<>();

        // Handle instance method: add receiver object
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
            receiverStmt = createConstructorStatement(cls, newSeq, pool, constructors);
            if (receiverStmt != null) {
              args.add(new Argument(receiverStmt));
            } else {
              System.err.println("Failed to create constructor for " + cls.getSimpleName());
              continue;
            }
          }
        }

        // Generate arguments
        for (Class<?> type : method.getParameterTypes()) {
          // 5% chance to use null for object types
          if (!type.isPrimitive() && random.nextDouble() < 0.05) {
            Statement nullStmt = new ConstantAssignment(null, type);
            newSeq.statements.add(nullStmt);
            args.add(new Argument(nullStmt));
            continue;
          }
          
          // 20% chance to use a random value regardless of usable statements
          if (random.nextDouble() < 0.2) {
            Statement paramStmt = createParameter(type, newSeq, pool);
            args.add(new Argument(paramStmt));
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
              Statement paramStmt = createParameter(type, newSeq, pool);
              args.add(new Argument(paramStmt));
            }
          }
        }
        
        newSeq.statements.add(new MethodCall(method, args));        
      }

      sequenceCount++;

      try {
        newSeq.execute();
      } catch (Exception e) {
        System.out.println("Exception during execution: " + e);
        System.out.println("Sequence that threw exception: " + newSeq.toCode(false));
        newSeq.setThrewException(true);
        errorSeqs.add(newSeq);
        continue;
      }
      
      // Check structural equivalence
      String fingerprint = newSeq.getSignatureFingerprint();
      if (seenFingerprints.contains(fingerprint)) {
        System.out.println("Sequence skipped due to duplicate structure");
        errorSeqs.add(newSeq);
        continue;
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
      Pair<String, Statement> contractResult = ContractChecker.checkStatements(newSeq.statements);
      String violatedContract = contractResult.first;
      Statement violatingStmt = contractResult.second;
      
      if (violatedContract != null) {
        System.out.println("Sequence violates contract: " + violatedContract);
        newSeq.setViolatedContract(violatedContract);
        newSeq.setViolatingStmt(violatingStmt);
        passedAll = false;
      }

      if (passedAll) {
        validSeqs.add(newSeq);
        seenFingerprints.add(fingerprint);
        pool.addSequence(newSeq);
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

  // Helper method to select a method using weighted random selection
  // Less used methods have higher probability of being selected
  private static Method getRandomMethod(List<Method> methods, Map<Method, Integer> methodUsageCount) {
    // Find the maximum usage count
    int maxUsage = 0;
    for (Method method : methods) {
      int usage = methodUsageCount.get(method);
      if (usage > maxUsage) {
        maxUsage = usage;
      }
    }

    // Methods with lower usage get higher weights
    List<Double> weights = new ArrayList<>();
    double totalWeight = 0.0;
    for (Method method : methods) {
      int usage = methodUsageCount.get(method);
      double weight = maxUsage - usage + 1.0;
      weights.add(weight);
      totalWeight += weight;
    }
    
    // Select randomly based on weights
    double randomValue = random.nextDouble() * totalWeight;
    double cumulativeWeight = 0.0;
    
    for (int i = 0; i < methods.size(); i++) {
      cumulativeWeight += weights.get(i);
      if (randomValue <= cumulativeWeight) {
        return methods.get(i);
      }
    }
    
    return methods.get(methods.size() - 1);
  }

  // Helper method to create a parameter statement for a given type
  private static Statement createParameter(Class<?> type, Sequence newSeq, SequencePool pool) {
    // If needed, create a constructor call
    if (!type.isPrimitive() && type != String.class && !type.isArray() && type != List.class) {
      // Get constructors for this type
      List<Constructor<?>> typeConstructors = new ArrayList<>();
      for (Constructor<?> constructor : type.getDeclaredConstructors()) {
        if (Modifier.isPublic(constructor.getModifiers())) {
          typeConstructors.add(constructor);
        }
      }
      
      if (!typeConstructors.isEmpty()) {
        Statement constructorStmt = createConstructorStatement(type, newSeq, pool, typeConstructors);
        if (constructorStmt != null) {
          return constructorStmt;
        }
      }
    }
    
    // Otherwise create random primitive
    Object randomValue = getRandomValue(type);
    Statement constantStmt = new ConstantAssignment(randomValue, type);
    newSeq.statements.add(constantStmt);
    return constantStmt;
  }

  // Helper method to create a constructor statement for a given class
  private static Statement createConstructorStatement(Class<?> cls, Sequence newSeq, SequencePool pool, List<Constructor<?>> constructors) {
    try {
      if (constructors.isEmpty()) {
        return null;
      }
      
      // Pick a random constructor
      Constructor<?> constructor = constructors.get(random.nextInt(constructors.size()));
      
      // Generate arguments for the constructor
      List<Argument> constructorArgs = new ArrayList<>();
      
      for (Class<?> paramType : constructor.getParameterTypes()) {
          // 10% chance to use a random value regardless of usable statements
          if (random.nextDouble() < 0.1) {
            Object randomValue = getRandomValue(paramType);
            Statement constantStmt = new ConstantAssignment(randomValue, paramType);
            newSeq.statements.add(constantStmt);
            constructorArgs.add(new Argument(constantStmt));
            continue;
          }

        // Check if current sequence contains usable statement
        Statement argStmt = pool.findStatementOfType(newSeq, paramType);
        if (argStmt != null) {
          constructorArgs.add(new Argument(argStmt));
        } else {
          // Otherwise, check pool
          Sequence argSequence = pool.findSequenceOfType(paramType);
          if (argSequence != null) {
            newSeq.concat(argSequence);
            argStmt = pool.findStatementOfType(argSequence, paramType);
            constructorArgs.add(new Argument(argStmt));
          } else {
            Object randomValue = getRandomValue(paramType);
            Statement constantStmt = new ConstantAssignment(randomValue, paramType);
            newSeq.statements.add(constantStmt);
            constructorArgs.add(new Argument(constantStmt));
          }
        }
      }
      
      Statement constructorStmt = new ConstructorCall(constructor, constructorArgs);
      newSeq.statements.add(constructorStmt);
      constructorStmt.execute();
      
      return constructorStmt;
    } catch (Exception e) {
      System.err.println("Failed to create constructor for " + cls.getSimpleName() + ": " + e.getMessage());
      return null;
    }
  }

}