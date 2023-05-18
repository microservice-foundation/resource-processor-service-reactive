package com.epam.training.microservicefoundation.resourceprocessor.service;

public interface Convertor<O, I> {
  O covert(I input);
}
