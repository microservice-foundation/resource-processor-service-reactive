package com.epam.training.microservicefoundation.resourceprocessor.domain.context;

import com.epam.training.microservicefoundation.resourceprocessor.domain.dto.GetSongDTO;
import com.epam.training.microservicefoundation.resourceprocessor.domain.dto.SaveSongDTO;
import java.nio.file.Path;
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
  private Path resourceFilePath;
  private SaveSongDTO saveSongDTO;
  private GetSongDTO getSongDTO;
}
