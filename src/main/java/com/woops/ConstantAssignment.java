package com.woops;

import java.util.Arrays;

public class ConstantAssignment extends Statement {

  public ConstantAssignment(Object value, Class<?> type) {
    super(type); // initialize type
    this.result = value; 
  }

  @Override
  public void execute() {
    // no-op: already assigned at construction
  }

  @Override
  public String toCode() {
    StringBuilder code = new StringBuilder();

    // Format the type name properly
    String typeName = getFormattedTypeName(this.getType());
    String valueString = getFormattedValue(result);

    // Save the result to a variable
    code.append(typeName)
      .append(" ")
      .append(getVariableName())
      .append(" = ")
      .append(valueString);

    return code.toString();
  }

  // Make sure Array types are handled correctly
  private String getFormattedTypeName(Class<?> type) {
    if (type.isArray()) {
      Class<?> componentType = type.getComponentType();
      return getFormattedTypeName(componentType) + "[]";
    }
    return type.getSimpleName();
  }

  private String getFormattedValue(Object value) {
    if (value == null) {
      return "null";
    }
    
    // Handle different array types
    if (value.getClass().isArray()) {
      if (value instanceof int[]) {
        return "new int[]" + Arrays.toString((int[]) value);
      } else if (value instanceof String[]) {
        String[] stringArray = (String[]) value;
        StringBuilder sb = new StringBuilder("new String[]{");
        for (int i = 0; i < stringArray.length; i++) {
          if (i > 0) sb.append(", ");
          sb.append(addQuotes(stringArray[i]));
        }
        sb.append("}");
        return sb.toString();
      } else if (value instanceof boolean[]) {
        return "new boolean[]" + Arrays.toString((boolean[]) value);
      } else if (value instanceof char[]) {
        char[] charArray = (char[]) value;
        StringBuilder sb = new StringBuilder("new char[]{");
        for (int i = 0; i < charArray.length; i++) {
          if (i > 0) sb.append(", ");
          sb.append(addQuotes(charArray[i]));
        }
        sb.append("}");
        return sb.toString();
      } else if (value instanceof Object[]) {
        Object[] objArray = (Object[]) value;
        StringBuilder sb = new StringBuilder("new " + getFormattedTypeName(value.getClass().getComponentType()) + "[]{");
        for (int i = 0; i < objArray.length; i++) {
          if (i > 0) sb.append(", ");
          sb.append(getFormattedValue(objArray[i]));
        }
        sb.append("}");
        return sb.toString();
      } else {
        // Handle other primitive arrays
        return "new " + getFormattedTypeName(value.getClass()) + Arrays.toString((Object[]) value);
      }
    }
    
    if (value instanceof String || value instanceof Character) {
      return (String) addQuotes(value);
    }
    
    return value.toString();
  }

  // For equivalence filtering
  @Override
  public String getSignature() {
    return "const(" + (result == null ? "null" : result.getClass().getSimpleName()) + ")";
  }
}
