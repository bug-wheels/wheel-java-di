package com.github.sbb.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil {

  private ReflectUtil() {
  }

  public static boolean trySetAccessible(Constructor constructor) {
    if (constructor.isAccessible()) {
      return true;
    }
    constructor.setAccessible(true);
    return true;
  }

  public static boolean trySetAccessible(Method method) {
    if (method.isAccessible()) {
      return true;
    }
    method.setAccessible(true);
    return true;
  }

  public static boolean trySetAccessible(Field field) {
    if (field.isAccessible()) {
      return true;
    }
    field.setAccessible(true);
    return true;
  }
}
