package org.devoware.reactive.property;

public class CyclicBindingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CyclicBindingException(String message) {
    super(message);
  }

  public CyclicBindingException() {
    super();
  }
  
}
