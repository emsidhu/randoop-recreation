package com.woops;

// helper class for checking null exeptions
public final class Invocation {

  private final java.lang.reflect.Executable executable; // Method or Constructor
  private final Object[] args;
  private final Object   returnValue;   // null if void or an exception was thrown
  private final Throwable exception;    // null when the call completed normally

  public Invocation(java.lang.reflect.Executable executable,
                    Object[] args,
                    Object returnValue,
                    Throwable exception) {
    this.executable  = executable;
    this.args        = args;
    this.returnValue = returnValue;
    this.exception   = exception;
  }

  public java.lang.reflect.Executable getExecutable() { return executable; }
  public Object[]  getArgs()     { return args; }
  public Object    getReturn()   { return returnValue; }
  public Throwable getException(){ return exception; }
}
