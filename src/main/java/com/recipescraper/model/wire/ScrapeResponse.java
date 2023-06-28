package com.recipescraper.model.wire;

import com.recipescraper.model.Recipe;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Builder
@Data
@Jacksonized
public class ScrapeResponse {
  private Recipe recipe;
}
