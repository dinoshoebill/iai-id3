package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Parser {

    public static Map<String, Integer> buildFeaturesIndexMap(List<String> features) {
        Map<String, Integer> featuresMap = new HashMap<>();
        for (int i = 0; i < features.size(); i++) {
            featuresMap.put(features.get(i), i);
        }
        return featuresMap;
    }

    public static List<String> parseFeatures(String fileName) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            List<String> features = new ArrayList<>();
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine().replaceAll("\\s", "");
                features.addAll(Arrays.asList(line.split(",")));
                features.remove(features.size() - 1);
            }
            return features;
        }
    }

    public static Map<String, List<String>> parseFeatureAttributes(Tree struct) {
        Map<String, List<String>> attributes = new HashMap<>();
        List<Set<String>> attributeSets = new ArrayList<>(struct.features.size());
        for (int i = 0; i < struct.features.size(); i++) {
            attributeSets.add(new LinkedHashSet<>());
        }

        for (List<String> entry : struct.trainingData) {
            for (int i = 0; i < entry.size() - 1; i++) {
                attributeSets.get(i).add(entry.get(i));
            }
        }

        for (int i = 0; i < struct.features.size(); i++) {
            List<String> sorted = new ArrayList<>(attributeSets.get(i));
            Collections.sort(sorted);
            attributes.put(struct.features.get(i), sorted);
        }

        return attributes;
    }

    public static List<List<String>> parseCSV(String fileName) throws IOException {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            List<List<String>> rows = new ArrayList<>();

            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().replaceAll("\\s", "");
                rows.add(Arrays.asList(line.split(",")));
            }

            return rows;
        }
    }

    public static List<String> parseClassLabels(Tree struct) {
        Set<String> uniqueClassLabels = new HashSet<>();
        int classLabelColumn = struct.trainingData.get(0).size() - 1;

        for (List<String> entry : struct.trainingData) {
            uniqueClassLabels.add(entry.get(classLabelColumn));
        }

        List<String> labels = new ArrayList<>(uniqueClassLabels);
        Collections.sort(labels);
        return labels;
    }
}
