package com.github.sbb.di.entity;

import javax.inject.Singleton;

@Singleton
public class Node {

  @Override
  public String toString() {
    return " Node";
  }

  public void send(String message) {
    System.out.println(" Node send " + message);
  }
}
