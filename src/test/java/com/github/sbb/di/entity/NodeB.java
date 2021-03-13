package com.github.sbb.di.entity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NodeB {

  @Inject
  private Node node;

  public void nodeSend(String message) {
    node.send(message);
  }

  @Override
  public String toString() {
    return " Node B";
  }
}
