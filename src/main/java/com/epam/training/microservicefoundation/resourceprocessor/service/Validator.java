package com.epam.training.microservicefoundation.resourceprocessor.service;

public interface Validator<T> {
    boolean validate(T input);
}
