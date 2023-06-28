package com.recipescraper.controller;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.recipescraper.model.Recipe;
import com.recipescraper.model.wire.ScrapeRequest;
import com.recipescraper.model.wire.ScrapeResponse;
import com.recipescraper.service.ScrapeService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ScrapeController {
  private final ScrapeService scrapeService;

  @PostMapping("/scrape")
  public ResponseEntity<ScrapeResponse> getRecipeJSON(@NonNull @RequestBody ScrapeRequest request)
      throws UnirestException {
    HttpResponse<String> response;
    try {
      response = Unirest.get(request.getUrl()).asString();
    } catch (UnirestException e) {
      throw new UnirestException(e);
    }
    Recipe found = scrapeService.findRecipeInHtml(response);
    return ResponseEntity.ok(ScrapeResponse.builder().recipe(found).build());
  }
}
