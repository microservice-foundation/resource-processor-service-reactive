package com.epam.training.microservicefoundation.resourceprocessor.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongDTO implements Serializable {
  private static final long serialVersionUID = 17_11_2022_22_51L;
  private long id;
}
