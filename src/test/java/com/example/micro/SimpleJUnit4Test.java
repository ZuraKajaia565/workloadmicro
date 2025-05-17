// src/test/java/com/example/micro/SimpleJUnit4Test.java
package com.example.micro;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class SimpleJUnit4Test {

    @Test
    public void simpleTest() {
        assertTrue(true);
    }
}