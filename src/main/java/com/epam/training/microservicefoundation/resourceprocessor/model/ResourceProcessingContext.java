package com.epam.training.microservicefoundation.resourceprocessor.model;

import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@With
public class ResourceProcessingContext {
  private long resourceId;
  private File resourceFile;
  private SongMetadata songMetadata;

  public ResourceProcessingContext(long resourceId) {
    this.resourceId = resourceId;
  }
}
