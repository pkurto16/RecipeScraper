package com.recipescraper.model.wire;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Builder
@Data
@Jacksonized
public class ScrapeRequest {
  private String url;
}
