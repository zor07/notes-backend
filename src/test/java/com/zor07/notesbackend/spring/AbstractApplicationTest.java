package com.zor07.notesbackend.spring;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

abstract public class AbstractApplicationTest {

  @SpringBootApplication(scanBasePackages = "com.zor07.notesbackend")
  static class TestApp {
  }

  private static volatile ConfigurableApplicationContext CONTEXT;

  @AfterMethod
  protected void afterMethod() {
  }

  @AfterSuite
  final protected void afterSuite() {
    CONTEXT.close();
    CONTEXT = null;
  }

  @BeforeClass
  protected void beforeClass() {
    CONTEXT.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @BeforeSuite
  final protected void beforeSuite() {
    CONTEXT = new SpringApplicationBuilder(TestApp.class)
        .profiles("test", "test-local")
        .web(WebApplicationType.SERVLET)
        .run();
  }

}
