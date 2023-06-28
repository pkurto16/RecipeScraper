package com.recipescraper.service;

import com.mashape.unirest.http.HttpResponse;
import com.recipescraper.model.Recipe;
import com.recipescraper.util.HtmlCleaningUtils;
import com.recipescraper.util.JsonParsingUtils;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ScrapeService {
  public Recipe findRecipeInHtml(HttpResponse<String> response) {

    Document content = Jsoup.parse(response.getBody());
    Map<String, Object> jsonMap = mapFromRawHtmlContent(content.data());
    if (jsonMap == null) {
      System.out.println("dang");
      return null;
    }
    return findRecipe(jsonMap);
  }

  private Map<String, Object> mapFromRawHtmlContent(String htmlSectionContainingJson) {
    if (stringInvalid(htmlSectionContainingJson)) {
      return null;
    }
    JacksonJsonParser parser = new JacksonJsonParser();
    return parser.parseMap(HtmlCleaningUtils.findJsonStringFromHtml(htmlSectionContainingJson));
  }

  private boolean stringInvalid(String htmlSectionContainingJson) {
    return htmlSectionContainingJson == null
        || !htmlSectionContainingJson.contains("\"@context\":")
        || (!htmlSectionContainingJson.contains("\"https://schema.org\"")
            && !htmlSectionContainingJson.contains("\"http://schema.org\""))
        || !htmlSectionContainingJson.contains("\"@type\":")
        || !htmlSectionContainingJson.contains("\"Recipe\"");
  }

  private Recipe findRecipe(Map<String, Object> jsonMap) {
    Object ingredients = JsonParsingUtils.findElementInJsonMap("recipeIngredient", jsonMap);
    Object instructions = JsonParsingUtils.findElementInJsonMap("recipeInstructions", jsonMap);
    return Recipe.builder().ingredients(ingredients).instructions(instructions).build();
  }
}
