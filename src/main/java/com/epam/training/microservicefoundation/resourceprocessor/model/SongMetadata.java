package com.epam.training.microservicefoundation.resourceprocessor.model;

import java.io.Serializable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SongMetadata implements Serializable {
  private static final long serialVersionUID = 2022_10_24_19_44L;
  long resourceId;
  String name;
  String artist;
  String album;
  String length;
  int year;
}
