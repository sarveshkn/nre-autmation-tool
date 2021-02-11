package com.amadeus.ghostingutils.utils;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * FlatMap Utility class used for comparing unflattened objects from json requests and extracting input data variables
 */
public final class FlatMapUtil {

  /**
   * private constructor to hide the explicit public one
   */
  private FlatMapUtil() {
    throw new AssertionError("No instances for you for FlatMapUtil!");
  }

  /**
   * Entry method for flattening object maps
   *
   * @param stringObjectMap Map<String, Object>
   * @return Map<String, Object>
   */
  public static Map<String, Object> flatten(Map<String, Object> stringObjectMap) {
    return stringObjectMap.entrySet()
        .stream()
        .flatMap(FlatMapUtil::flatten)
        .collect(LinkedHashMap::new, (m, e) -> m.put("/" + e.getKey(), e.getValue()), LinkedHashMap::putAll);
  }

  /**
   * Flattening logic for nested object maps
   *
   * @param nonFlattenedEntry Map.Entry<String, Object>
   * @return Stream<Map.Entry < String, Object>>
   */
  private static Stream<Map.Entry<String, Object>> flatten(Map.Entry<String, Object> nonFlattenedEntry) {

    if (nonFlattenedEntry == null) {
      return Stream.empty();
    }

    // If Object contains a nested Map inside
    if (nonFlattenedEntry.getValue() instanceof Map<?, ?>) {
      return ((Map<?, ?>)nonFlattenedEntry.getValue()).entrySet()
          .stream()
          .flatMap(
              e -> flatten(new AbstractMap.SimpleEntry<>(nonFlattenedEntry.getKey() + "/" + e.getKey(), e.getValue())));
    }

    // If object contains a list inside
    if (nonFlattenedEntry.getValue() instanceof List<?>) {
      List<?> list = (List<?>)nonFlattenedEntry.getValue();
      return IntStream.range(0, list.size())
          .mapToObj(i -> new AbstractMap.SimpleEntry<String, Object>(nonFlattenedEntry.getKey() + "/" + i, list.get(i)))
          .flatMap(FlatMapUtil::flatten);
    }

    return Stream.of(nonFlattenedEntry);
  }
}