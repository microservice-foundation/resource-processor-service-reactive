package com.epam.training.microservicefoundation.resourceprocessor.web.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Server {

  Service service() default Service.NONE;
  enum Service {
    RESOURCE,
    SONG,
    NONE
  }
}
