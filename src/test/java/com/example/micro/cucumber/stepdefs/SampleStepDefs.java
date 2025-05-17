// src/test/java/com/example/micro/cucumber/stepdefs/SampleStepDefs.java
package com.example.micro.cucumber.stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.Assert.assertTrue;

public class SampleStepDefs {

    @Given("I have a simple test")
    public void iHaveASimpleTest() {
        System.out.println("Given step executed");
    }

    @When("I run the test")
    public void iRunTheTest() {
        System.out.println("When step executed");
    }

    @Then("the test should pass")
    public void theTestShouldPass() {
        System.out.println("Then step executed");
        assertTrue(true);
    }
}