package com.recipescraper.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Data
@Jacksonized
public class Recipe {
  private Object ingredients;
  private Object instructions;
}
