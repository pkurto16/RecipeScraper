package com.recipescraper.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
//look up auto import
@Builder
@Data
@Jacksonized
public class ScrapeRequest {
  private String url;
}
