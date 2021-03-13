package com.github.sbb.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

public class Injector {

  // 已经生成的单例
  private final Map<Class<?>, Object> finalSingletonMap = Collections.synchronizedMap(new HashMap<>());

  // 准备进行构造的类
  private final Set<Class<?>> processingInstances = Collections.synchronizedSet(new HashSet<>());

  /**
   * 获取对象
   */
  public <T> T getInstance(Class<T> clazz) {
    return createNew(clazz);
  }


  @SuppressWarnings("unchecked")
  private <T> T createNew(Class<T> clazz) {
    Object o = finalSingletonMap.get(clazz);
    if (o != null) {
      return (T) o;
    }

    ArrayList<Constructor<T>> constructors = new ArrayList<>();
    T target;
    for (Constructor<?> con : clazz.getDeclaredConstructors()) {
      // 优先处理 Inject 构造器
      if (con.isAnnotationPresent(Inject.class) && ReflectUtil.trySetAccessible(con)) {
        constructors.add(0, (Constructor<T>) con);
      } else if (con.getParameterCount() == 0 && ReflectUtil.trySetAccessible(con)) {
        constructors.add((Constructor<T>) con);
      }
    }

    if (constructors.size() > 2) {
      // 如果大于 2 说明存在多个被 Inject 标记的构造器，此时无确定优先使用哪个
      throw new InjectException("无法确定使用哪个构造器进行构造 " + clazz.getCanonicalName());
    }
    if (constructors.size() == 0) {
      // 如果不存在可用的构造器，则无法构造
      throw new InjectException("无可用的构造器 " + clazz.getCanonicalName());
    }
    processingInstances.add(clazz); // 放入表示未完成的容器

    target = createFromConstructor(constructors.get(0)); // 构造器注入

    injectField(target);
    injectMethod(target);

    processingInstances.remove(clazz); // 从未完成的容器取出

    boolean isSingleton = clazz.isAnnotationPresent(Singleton.class);
    if (isSingleton) {
      finalSingletonMap.put(clazz, target);
    }

    return target;
  }

  private <T> T createFromConstructor(Constructor<T> con) {
    Object[] params = new Object[con.getParameterCount()];
    int i = 0;
    for (Parameter parameter : con.getParameters()) {
      if (processingInstances.contains(parameter.getType())) {
        throw new InjectException(
            String.format("循环依赖 on constructor, class is %s", con.getDeclaringClass().getCanonicalName()));
      }
      Object param = createFromParameter(parameter);
      params[i++] = param;
    }
    try {
      return con.newInstance(params);
    } catch (Exception e) {
      throw new InjectException("create instance from constructor error", e);
    }
  }

  /**
   * 注入 Field
   */
  private <T> void injectField(T body) {
    List<Field> fields = new ArrayList<>();
    for (Field field : body.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(Inject.class) && ReflectUtil.trySetAccessible(field)) {
        fields.add(field);
      }
    }
    for (Field field : fields) {
      Object f = createFromField(field);
      try {
        field.set(body, f);
      } catch (Exception e) {
        throw new InjectException(
            String.format("set field for %s@%s error", body.getClass().getCanonicalName(), field.getName()),
            e);
      }
    }
  }

  private <T> void injectMethod(T body) {
    List<Method> methods = new ArrayList<>();
    Method[] declaredMethods = body.getClass().getDeclaredMethods();
    for (Method declaredMethod : declaredMethods) {
      if (declaredMethod.getParameterCount() > 0
          && declaredMethod.isAnnotationPresent(Inject.class)
          && ReflectUtil.trySetAccessible(declaredMethod)) {
        methods.add(declaredMethod);
      }
    }

    int i;
    for (Method method : methods) {
      Object[] params = new Object[method.getParameterCount()];
      i = 0;
      for (Parameter parameter : method.getParameters()) {
        if (processingInstances.contains(parameter.getType())) {
          throw new InjectException(
              String.format("循环依赖 on method , the root class is %s", body.getClass().getCanonicalName()));
        }
        Object param = createFromParameter(parameter);
        params[i++] = param;
      }
      try {
        method.invoke(body, params);
      } catch (Exception e) {
        throw new InjectException("injectMethod ", e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T createFromParameter(Parameter parameter) {
    Class<?> clazz = parameter.getType();
    return (T) createNew(clazz);
  }

  @SuppressWarnings("unchecked")
  private <T> T createFromField(Field field) {
    Class<?> clazz = field.getType();
    return (T) createNew(clazz);
  }

}
