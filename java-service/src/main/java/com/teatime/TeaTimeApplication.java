package com.teatime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableJpaRepositories(basePackages = "com.teatime.repository")
@SpringBootApplication
public class TeaTimeApplication {

  public static void main(String[] args) {
    SpringApplication.run(TeaTimeApplication.class, args);
  }

}
