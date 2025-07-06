package com.woops;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SequencePool {
  private final List<Sequence> allSequences = new ArrayList<>();
  private final Map<Class<?>, List<Sequence>> typeIndex = new HashMap<>();
  private final Random random = new Random();

  public void addSequence(Sequence seq) {
    allSequences.add(seq);

    // Map the sequence to all the types it contains
    for (Statement stmt : seq.statements) {
      Class<?> type = stmt.getType();
      if (type != void.class) {
        typeIndex.computeIfAbsent(type, k -> new ArrayList<>()).add(seq);
      }
    }
  }

  // Returns a Sequence containing a Statement with a return value of targetType
  public Sequence findSequenceOfType(Class<?> targetType) {
    List<Sequence> candidates = typeIndex.get(targetType);
    if (candidates == null) candidates = new ArrayList<>();

    // If subclasses are possible, check for them
    if (
      !targetType.isPrimitive() && 
      !targetType.isArray() &&
      !Modifier.isFinal(targetType.getModifiers())
    ) {
      for (Map.Entry<Class<?>, List<Sequence>> entry : typeIndex.entrySet()) {
        if (targetType.isAssignableFrom(entry.getKey())) {
          candidates.addAll(entry.getValue());
        }
      }
    }

    if (candidates.isEmpty()) {
      return null;
    }
    // Return a random candidate
    return candidates.get(random.nextInt(candidates.size()));
  }

  // Returns the statement in seq with return type of targetType
  public Statement findStatementOfType(Sequence seq, Class<?> targetType) {
    // Get all statements of targetType from the sequence
    List<Statement> candidates = new ArrayList<>();
    for (Statement stmt : seq.statements) {
      if (targetType.isAssignableFrom(stmt.getType())) {
        candidates.add(stmt);
      }
    }
    
    if (candidates.isEmpty()) {
      return null;
    }
    
    // Return a random candidate
    return candidates.get(random.nextInt(candidates.size()));
  }
}

