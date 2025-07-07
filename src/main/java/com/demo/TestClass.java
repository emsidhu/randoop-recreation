package com.demo;

public class TestClass {

  public TestClass() {}

  public static int add(int x, int y) {
    return x + y;
  }

  public static String repeat(String s, int times) {
      return s.repeat(times);
  }

  public static int divide(int a, int b) {
    return a / b; // Should be filtered by ExceptionFilter if b is 0
  }

  public static Object returnNull() {
    return null; // Should be filtered by NullFilter
  }

  public static String echo(String s) {
    return s; // Repeated return values may be filtered by EqualityFilter
  }
  
  public static String echoHello() {
    return "hello"; // Multiple calls may be filtered by EqualityFilter
  }

  public static int divideByZero() {
    return 10 / 0; // Always throws ArithmeticException → triggers ExceptionFilter
  }

}
