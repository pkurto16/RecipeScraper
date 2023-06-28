package com.recipescraper.util;

public class HtmlCleaningUtils {
  public static String findJsonStringFromHtml(String json) {
    int recipeIndex = findRecipeJsonIndex(json);
    json = json.substring(recipeIndex);
    return cleanEndOfJson(json);
  }

  private static int findRecipeJsonIndex(String json) {
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

  private static int findCorrectStartIndex(String json) {
    String context = "\"@context\":";
    String schemaHttps = "\"https://schema.org\"";
    String schemaHttp = "\"http://schema.org\"";
    String type = "\"@type\":";
    String recipe = "\"Recipe\"";
    String fullStringHttps = context + schemaHttps + ',' + type + recipe;
    String fullStringHttp = context + schemaHttp + ',' + type + recipe;
    int recipeJsonStartIndex = json.indexOf(context);
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
        recipeJsonStartIndex = json.indexOf(context, recipeJsonStartIndex + distance + spaces) + 1;
        distance = 0;
        spaces = 0;
        usingHttp = false;
      }
    }
    return recipeJsonStartIndex;
  }

  private static String cleanEndOfJson(String json) {
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
}
