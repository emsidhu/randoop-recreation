package com.demo;

public class TestClass {
  public TestClass() {}

  public int add(int x, int y) {
    return x + y;
  }

  public static String repeat(String s, int times) {
      return s.repeat(times);
  }

  public static void crash() {
      throw new RuntimeException("Oops");
  }
}
