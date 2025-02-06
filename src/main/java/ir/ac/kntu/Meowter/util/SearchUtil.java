package ir.ac.kntu.Meowter.util;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;
import java.util.stream.Collectors;

public class SearchUtil {

    private static final int SIMILARITY_THRESHOLD = 50;
    public static List<String> search(String query, List<String> data) {
        LevenshteinDistance distance = new LevenshteinDistance();
        return data.stream()
                .map(item -> new AbstractMap.SimpleEntry<>(item, similarity(query, item, distance)))
                .filter(entry -> entry.getValue() >= SIMILARITY_THRESHOLD)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(AbstractMap.SimpleEntry::getKey)
                .collect(Collectors.toList());
    }

    private static int similarity(String query, String item, LevenshteinDistance distance) {
        int maxLength = Math.max(query.length(), item.length());
        int dist = distance.apply(query, item);
        return 100 - (dist * 100 / maxLength);
    }
}
