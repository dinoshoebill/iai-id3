package ui;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ID3 {

    public static void fit(Tree struct) {
        struct.root = buildDecisionTree(
                struct,
                struct.trainingData,
                new HashSet<>(struct.features),
                null,
                0);
    }

    public static Node buildDecisionTree(
            Tree struct,
            List<List<String>> currentDataSet,
            Set<String> usedFeaturesSet,
            String parentMostCommonClassLabel,
            int depth) {

        Node node = new Node();
        node.entropy = entropy(
                struct,
                countClassLabelsInDataSet(struct, currentDataSet),
                currentDataSet.size());
        node.depth = depth;

        if (node.entropy == 0) {
            if (!currentDataSet.isEmpty()) {
                node.feature = calcMostCommonDatasetLabel(struct, currentDataSet);
            } else {
                node.feature = parentMostCommonClassLabel;
            }
            node.attribute = node.feature;
            node.mostCommonClassLabel = node.feature;
            return node;
        }

        if (usedFeaturesSet.isEmpty()) {
            node.feature = parentMostCommonClassLabel;
            node.attribute = parentMostCommonClassLabel;
            node.mostCommonClassLabel = parentMostCommonClassLabel;
            return node;
        }

        node.mostCommonClassLabel = calcMostCommonDatasetLabel(struct, currentDataSet);

        Map<String, Map<String, List<List<String>>>> featureAttributeSubSet = new HashMap<>();

        for (Map.Entry<String, List<String>> attributes : struct.featuresAttributes.entrySet()) {
            if (!usedFeaturesSet.contains(attributes.getKey())) {
                continue;
            }

            Map<String, List<List<String>>> attributeSubSet = new HashMap<>();
            for (String attribute : attributes.getValue()) {
                attributeSubSet.put(
                        attribute,
                        getAttributeSubset(struct, currentDataSet, attribute, attributes.getKey()));
            }
            featureAttributeSubSet.put(attributes.getKey(), attributeSubSet);
        }

        AbstractMap.SimpleImmutableEntry<String, Double> bestDiscriminativeFeature = calcBestIG(
                struct,
                featureAttributeSubSet,
                currentDataSet.size(),
                usedFeaturesSet,
                node.entropy);

        usedFeaturesSet.remove(bestDiscriminativeFeature.getKey());

        node.feature = bestDiscriminativeFeature.getKey();
        node.ig = bestDiscriminativeFeature.getValue();

        for (String attribute : struct.featuresAttributes.get(node.feature)) {
            Node child = buildDecisionTree(
                    struct,
                    featureAttributeSubSet.get(node.feature).get(attribute),
                    new HashSet<>(usedFeaturesSet),
                    node.mostCommonClassLabel,
                    depth + 1);
            child.attribute = attribute;
            child.parent = node;
            node.children.add(child);
        }
        return node;
    }

    public static AbstractMap.SimpleImmutableEntry<String, Double> calcBestIG(
            Tree struct,
            Map<String, Map<String, List<List<String>>>> featureAttributeSubSet,
            int currentDataSetSize,
            Set<String> usedFeaturesSet,
            double nodeEntropy) {

        Map<String, Double> featureIGMap = new HashMap<>();

        for (Map.Entry<String, Map<String, List<List<String>>>> feature : featureAttributeSubSet.entrySet()) {
            if (!usedFeaturesSet.contains(feature.getKey())) {
                continue;
            }

            double ig = nodeEntropy;
            for (Map.Entry<String, List<List<String>>> attribute : feature.getValue().entrySet()) {
                List<List<String>> subset = attribute.getValue();
                double attributeEntropy = entropy(
                        struct,
                        countClassLabelsInDataSet(struct, subset),
                        subset.size());
                ig -= attributeEntropy * ((double) subset.size() / currentDataSetSize);
            }
            featureIGMap.put(feature.getKey(), ig);
        }

        double maxIG = -1;
        String nodeFeature = null;
        for (Map.Entry<String, Double> igEntry : featureIGMap.entrySet()) {
            if (igEntry.getValue() > maxIG) {
                maxIG = igEntry.getValue();
                nodeFeature = igEntry.getKey();
            } else if (igEntry.getValue() == maxIG && nodeFeature.compareTo(igEntry.getKey()) > 0) {
                nodeFeature = igEntry.getKey();
            }
        }

        return new AbstractMap.SimpleImmutableEntry<>(nodeFeature, maxIG);
    }

    public static List<Integer> countClassLabelsInDataSet(Tree struct, List<List<String>> currentDataSet) {
        List<Integer> labelCounts = new ArrayList<>(Collections.nCopies(struct.classLabels.size(), 0));
        int labelPosition = struct.classLabelPosition;

        for (List<String> row : currentDataSet) {
            String label = row.get(labelPosition);
            for (int i = 0; i < struct.classLabels.size(); i++) {
                if (struct.classLabels.get(i).equals(label)) {
                    labelCounts.set(i, labelCounts.get(i) + 1);
                    break;
                }
            }
        }

        return labelCounts;
    }

    public static List<List<String>> getAttributeSubset(
            Tree struct,
            List<List<String>> currentDataSet,
            String attribute,
            String feature) {

        int featureIndex = struct.featuresIndexMap.get(feature);
        List<List<String>> subset = new ArrayList<>();
        for (List<String> entry : currentDataSet) {
            if (entry.get(featureIndex).equals(attribute)) {
                subset.add(entry);
            }
        }
        return subset;
    }

    public static void predict(Tree struct) {
        printBranches(struct.root, struct.depth);

        StringBuilder predictions = new StringBuilder();
        for (List<String> testRow : struct.testData) {
            struct.result.add(makePrediction(struct, struct.root, testRow));
            predictions.append(' ').append(struct.result.get(struct.result.size() - 1));
        }

        System.out.print("[PREDICTIONS]:");
        System.out.print(predictions);
        System.out.println();
    }

    public static String makePrediction(Tree struct, Node node, List<String> entry) {
        if (node.children.isEmpty()) {
            return node.feature;
        }
        if (node.depth == struct.depth) {
            return node.mostCommonClassLabel;
        }

        int featureIndex = struct.featuresIndexMap.get(node.feature);
        String entryValue = entry.get(featureIndex);
        for (Node child : node.children) {
            if (child.attribute.equals(entryValue)) {
                return makePrediction(struct, child, entry);
            }
        }

        return node.mostCommonClassLabel;
    }

    public static double entropy(Tree struct, List<Integer> classLabelCounts, int dataSetSize) {
        if (dataSetSize == 0) {
            return 0;
        }

        double logBase = 1 / Math.log10(struct.classLabels.size());
        double result = 0;

        for (int count : classLabelCounts) {
            double proportion = (double) count / dataSetSize;
            if (proportion != 0) {
                result -= proportion * Math.log10(proportion) * logBase;
            }
        }

        return result;
    }

    public static void printBranches(Node node, int depthLimit) {
        System.out.println("[BRANCHES]:");
        for (Node child : node.children) {
            printBranchesRecursive(child, depthLimit, "");
        }
    }

    public static void printBranchesRecursive(Node node, int depthLimit, String chain) {
        if (node.depth == depthLimit || node.children.isEmpty()) {
            System.out.println(chain + node.depth + ":" + node.parent.feature + "=" + node.attribute
                    + " " + node.mostCommonClassLabel);
            return;
        }

        String prefix = chain + node.depth + ":" + node.parent.feature + "=" + node.attribute + " ";
        for (Node child : node.children) {
            printBranchesRecursive(child, depthLimit, prefix);
        }
    }

    public static void calcAccuracy(Tree struct) {
        int correct = 0;
        int labelPosition = struct.classLabelPosition;
        for (int i = 0; i < struct.testData.size(); i++) {
            if (struct.testData.get(i).get(labelPosition).equals(struct.result.get(i))) {
                correct++;
            }
        }

        System.out.print("[ACCURACY]: ");
        System.out.printf(Locale.ROOT, "%.5f%n", (double) correct / struct.testData.size());
    }

    public static void confusionMatrix(Tree struct) {
        System.out.println("[CONFUSION_MATRIX]:");

        Map<String, Integer> rowIndex = new HashMap<>();
        for (int i = 0; i < struct.classLabels.size(); i++) {
            rowIndex.put(struct.classLabels.get(i), i);
        }
        Map<String, Integer> colIndex = new HashMap<>();
        for (int j = 0; j < struct.testClassLabels.size(); j++) {
            colIndex.put(struct.testClassLabels.get(j), j);
        }

        int rows = struct.classLabels.size();
        int cols = struct.testClassLabels.size();
        int[][] matrix = new int[rows][cols];
        int labelPosition = struct.classLabelPosition;

        for (int k = 0; k < struct.testData.size(); k++) {
            String actual = struct.testData.get(k).get(labelPosition);
            String predicted = struct.result.get(k);
            Integer row = rowIndex.get(actual);
            Integer col = colIndex.get(predicted);
            if (row != null && col != null) {
                matrix[row][col]++;
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (j == cols - 1) {
                    System.out.print(matrix[i][j]);
                } else {
                    System.out.print(matrix[i][j] + " ");
                }
            }
            System.out.println();
        }
    }

    public static String calcMostCommonDatasetLabel(Tree struct, List<List<String>> currentDataSet) {
        List<Integer> counts = countClassLabelsInDataSet(struct, currentDataSet);

        int max = -1;
        int index = 0;
        for (int i = 0; i < counts.size(); i++) {
            if (counts.get(i) > max) {
                max = counts.get(i);
                index = i;
            }
        }

        return struct.classLabels.get(index);
    }
}
