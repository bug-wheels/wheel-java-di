package com.github.sbb.di;

public class Injector {

  public <T> Injector registerSingleton(Class<T> clazz, T o) {
    return this;
  }


  public <T> Injector registerSingletonClass(Class<T> clazz) {
    return this;
  }

  public <T> T getInstance(Class<T> clazz) {
    return null;
  }
}
