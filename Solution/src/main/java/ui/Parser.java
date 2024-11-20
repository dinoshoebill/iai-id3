package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Parser {

    public static Map<String, Integer> parseFeaturesMap(String fileName) throws FileNotFoundException {

        Scanner scanner = new Scanner(new File(fileName));

        Map<String, Integer> featuresMap = new HashMap<>();
        List<String> featuresList;

        if(scanner.hasNextLine()) {
            String s = scanner.nextLine().replaceAll("\\s", "");
            featuresList = new ArrayList<>(Arrays.asList(s.split(",")));
            featuresList.remove(featuresList.size() - 1);

            int index = 0;
            for(String feature : featuresList) {
                featuresMap.put(feature, index);
                index++;
            }
        }
        return featuresMap;
    }

    public static List<String> parseFeatures(String fileName) throws FileNotFoundException {

        Scanner scanner = new Scanner(new File(fileName));

        List<String> features = new ArrayList<>();

        if(scanner.hasNextLine()) {
            String s = scanner.nextLine().replaceAll("\\s", "");
            features = new ArrayList<>(Arrays.asList(s.split(",")));
            features.remove(features.size() - 1);
        }

        return features;
    }

    public static Map<String, List<String>> parseFeatureAttributes(Tree struct) {

        Map<String, List<String>> attributes = new HashMap<>();

        List<List<String>> attributesSetList = new ArrayList<>();

        for(int i = 0; i < struct.features.size(); i++) {
            attributesSetList.add(new ArrayList<>());
        }

        for(List<String> entry : struct.trainingData) {
            for(int i = 0; i < entry.size() - 1; i++) {
                String s = entry.get(i);
                if(!attributesSetList.get(i).contains(s)) {
                    attributesSetList.get(i).add(s);
                }
            }
        }

        for(int i = 0; i < struct.features.size(); i++) {
            Collections.sort(attributesSetList.get(i));
            attributes.put(struct.features.get(i), attributesSetList.get(i));
        }

        return attributes;
    }

    public static List<List<String>> parseCSV(String fileName) throws IOException {

        Scanner scanner = new Scanner(new File(fileName));

        List<List<String>> trainingData = new ArrayList<>();

        if(scanner.hasNextLine()) {
            scanner.nextLine();
        }

        scanner.useDelimiter("[,\n]");

        while(scanner.hasNextLine()) {
            String s = scanner.nextLine().replaceAll("\\s", "");
            trainingData.add(Arrays.asList(s.split(",")));
        }

        scanner.close();

        return trainingData;
    }

    public static List<String> parseClassLabels(Tree struct) {

        Set<String> uniqueClassLabelsSet = new HashSet<>();
        int classLabelColumn = struct.trainingData.get(0).size() - 1;

        for(List<String> entry : struct.trainingData) {
            uniqueClassLabelsSet.add(entry.get(classLabelColumn));
        }

        List<String> uniqueClassLabelsList = new ArrayList<>(uniqueClassLabelsSet);

        Collections.sort(uniqueClassLabelsList);

        return uniqueClassLabelsList;
    }
}
