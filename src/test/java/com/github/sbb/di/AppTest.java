package com.github.sbb.di;

import static org.junit.Assert.assertTrue;

import com.github.sbb.di.entity.NodeB;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {


  /**
   * Rigorous Test :-)
   */
  @Test
  public void shouldAnswerWithTrue() {
    Injector injection = new Injector();

    NodeB instance = injection.getInstance(NodeB.class);

    instance.nodeSend("AppTest");

    assertTrue(true);
  }
}
