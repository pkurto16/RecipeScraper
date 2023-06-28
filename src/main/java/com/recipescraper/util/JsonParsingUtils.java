package com.recipescraper.util;

import java.util.ArrayList;
import java.util.Map;

public class JsonParsingUtils {
  public static Object findElementInJsonMap(String id, Object jsonMapPortion) {
    try {
      Map<String, Object> jsonMap = ((Map<String, Object>) jsonMapPortion);
      if (jsonMap.containsKey(id)) {
        return jsonMap.get(id);
      }
      return goThroughArrayLists(id, jsonMap);
    } catch (Exception notAMap) {
      try {
        Object objectOfId = checkArrayListElements(id, (ArrayList<Object>) jsonMapPortion);
        if (objectOfId != null) return objectOfId;
      } catch (Exception ignored) {
      }
    }
    return null;
  }

  private static Object goThroughArrayLists(String id, Map<String, Object> jsonMap) {
    for (Object o : jsonMap.values()) {
      try {
        Object objectOfId = checkArrayListElements(id, (ArrayList<Object>) o);
        if (objectOfId != null) {
          return objectOfId;
        }
      } catch (Exception notAnArrayList) {
        Object objectOfId = findElementInJsonMap(id, o);
        if (objectOfId != null) {
          return objectOfId;
        }
      }
    }
    return null;
  }

  private static Object checkArrayListElements(String id, ArrayList<Object> mapArrayList) {
    for (Object p : mapArrayList) {
      Object objectOfId = findElementInJsonMap(id, p);
      if (objectOfId != null) return objectOfId;
    }
    return null;
  }
}
