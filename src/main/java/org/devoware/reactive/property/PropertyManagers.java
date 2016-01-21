package org.devoware.reactive.property;

public class PropertyManagers {

  public static PropertyManager create () {
    return new BasicPropertyManager();
  }
  
  private PropertyManagers () {}
}
