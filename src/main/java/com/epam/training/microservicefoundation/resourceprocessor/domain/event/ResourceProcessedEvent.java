package com.epam.training.microservicefoundation.resourceprocessor.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceProcessedEvent {
  private static final long serialVersionUID = 21_07_2023_22_18L;
  private long id;
}
