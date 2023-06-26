package com.recipescraper;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.recipescraper.model.Recipe;
import com.recipescraper.model.ScrapeRequest;
import com.recipescraper.model.ScrapeResponse;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
//setting in IntelliJ to remove the lib.* (wildcard) import (set to max)
/*
Controller Classes: Post Mapping etc.
Service Classes: Logic/Functions i.e. recipeScraperService / JsonParse etc. called from controller classes
(if have db) Repository Classes: SQL Queries etc.
 */


@RestController
@RequestMapping
public class Main {
  @PostMapping("/scrape")
  public ResponseEntity<ScrapeResponse> getRecipeJSON(@RequestBody ScrapeRequest request) {
    return scrapedJsonFromUrl(request.getUrl());
  }

  private ResponseEntity<ScrapeResponse> scrapedJsonFromUrl(String url) {
    HttpResponse<String> response;
    try {
      response = Unirest.get(url).asString();
    } catch (UnirestException e) {
      throw new RuntimeException(e);
    }
    Document content = Jsoup.parse(response.getBody());
    Map<String, Object> jsonMap = mapFromRawData(content.data());
    if (jsonMap == null) {
      return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    Recipe recipe = findRecipe(jsonMap);
    return ResponseEntity.ok(ScrapeResponse.builder().recipe(recipe).build());
  }

  private Map<String, Object> mapFromRawData(String json) {
    Map<String, Object> jsonMap = new HashMap<>();

    String context = "\"@context\":";
    String schemaHttps = "\"https://schema.org\"";
    String schemaHttp = "\"http://schema.org\"";
    String type = "\"@type\":";
    String recipe = "\"Recipe\"";

    if (json == null) {
      return null;
    }
    if (json.contains(context)
        && (json.contains(schemaHttps) || json.contains(schemaHttp))
        && json.contains(type)
        && json.contains(recipe)) {

      return parseMapClean(json);
    }
    return null;
  }

  private Map<String, Object> parseMapClean(String json) {
    int recipeIndex = findRecipeJsonIndex(json);
    json = json.substring(recipeIndex);
    json = cleanEndOfJson(json);
    JacksonJsonParser j = new JacksonJsonParser();
    return j.parseMap(json);
  }

  private int findRecipeJsonIndex(String json) {
    // Note: using <script type="application/ld+json"> might be better as I can
    // also parse if @context is before @type but I left it like this for now
    int recipeJsonStartIndex = findCorrectStartIndex(json);
    if (recipeJsonStartIndex < 0) {
      return -1;
    }
    while (json.charAt(recipeJsonStartIndex) != '{' && recipeJsonStartIndex > 0) {
      recipeJsonStartIndex--;
    }
    return recipeJsonStartIndex;
  }

  private int findCorrectStartIndex(String json) {
    String context = "\"@context\":";
    String schemaHttps = "\"https://schema.org\"";
    String schemaHttp = "\"http://schema.org\"";
    String type = "\"@type\":";
    String recipe = "\"Recipe\"";
    String fullStringHttps = context + schemaHttps + ',' + type + recipe;
    String fullStringHttp = context + schemaHttp + ',' + type + recipe;
    int recipeJsonStartIndex = json.indexOf(context, 0);
    int distance = 0;
    int spaces = 0;
    boolean usingHttp = false;
    while (distance < fullStringHttps.length()
        && !(usingHttp && distance < fullStringHttp.length())
        && recipeJsonStartIndex >= 0) {

      if (json.charAt(recipeJsonStartIndex + distance + spaces) == fullStringHttps.charAt(distance)
          || distance < fullStringHttp.length()
              && json.charAt(recipeJsonStartIndex + distance + spaces)
                  == fullStringHttp.charAt(distance)) {
        if (distance < fullStringHttp.length()
            && distance > context.length() + schemaHttp.length()
            && json.charAt(recipeJsonStartIndex + distance + spaces)
                == fullStringHttp.charAt(distance)) {
          usingHttp = true;
        }
        distance++;
      } else if (json.charAt(recipeJsonStartIndex + distance + spaces) != '}') {
        spaces++;
      } else {
        recipeJsonStartIndex =
            json.indexOf(context, recipeJsonStartIndex + distance + spaces)
                + 1;
        distance = 0;
        spaces = 0;
        usingHttp = false;
      }
    }
    return recipeJsonStartIndex;
  }

  private String cleanEndOfJson(String json) {
    int i = 1;
    int curlyCount = 1;
    boolean inAString = false;
    boolean inAnEscape = false;
    while (curlyCount > 0) {
      if (!inAString && json.charAt(i) == '}') {
        curlyCount--;
      } else if (!inAString && json.charAt(i) == '{') {
        curlyCount++;
      } else if (!inAnEscape && json.charAt(i) == '"') {
        inAString = !inAString;
      }
      inAnEscape = json.charAt(i) == '\\';
      i++;
    }
    return json.substring(0, i);
  }

  private Recipe findRecipe(Map<String, Object> jsonMap) {
    Object ingredients = findRecipe("recipeIngredient", jsonMap);
    Object instructions = findRecipe("recipeInstructions", jsonMap);
    return Recipe.builder().ingredients(ingredients).instructions(instructions).build();
  }

  private JSONArray makeJsonArray(Object data) {
    JSONArray jsonArray = new JSONArray();
    if (data != null) {
      try {
        for (Object o : (Iterable<Object>) data) {
          jsonArray.put(o);
        }
      } catch (Exception ignored) {
        jsonArray.put(data);
      }
    }
    return jsonArray;
  }

  private Object findRecipe(String id, Object jsonPortion) {
    try {
      Map<String, Object> jsonMap = ((Map<String, Object>) jsonPortion);
      if (jsonMap.containsKey(id)) {
        return jsonMap.get(id);
      }
      return goThroughArrayLists(id, jsonMap);
    } catch (Exception notAMap) {
      try {
        Object objectOfId = checkArrayListElements(id, (ArrayList<Object>) jsonPortion);
        if (objectOfId != null) return objectOfId;
      } catch (Exception ignored) {
      }
    }
    return null;
  }

  private Object goThroughArrayLists(String id, Map<String, Object> jsonMap) {
    for (Object o : jsonMap.values()) {
      try {
        Object objectOfId = checkArrayListElements(id, (ArrayList<Object>) o);
        if (objectOfId != null){
          return objectOfId;
        }
      } catch (Exception notAnArrayList) {
        Object objectOfId = findRecipe(id, o);
        if (objectOfId != null) {
          return objectOfId;
        }
      }
    }
    return null;
  }

  private Object checkArrayListElements(String id, ArrayList<Object> mapArrayList) {
    for (Object p : mapArrayList) {
      Object objectOfId = findRecipe(id, p);
      if (objectOfId != null) return objectOfId;
    }
    return null;
  }
}
