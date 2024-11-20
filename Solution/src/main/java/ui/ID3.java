package ui;

import java.util.*;

public class ID3 {

    // train
    public static void fit(Tree struct) {
        struct.root = buildDecisionTree(struct, struct.trainingData, new HashSet<>(struct.features), null, 0); // define root node
    }

    // build tree recursively
    public static Node buildDecisionTree(Tree struct, List<List<String>> currentDataSet, Set<String> usedFeaturesSet, String parentMostCommonClassLabel, int depth) {

        Node node = new Node(); // define new node
        node.entropy = entropy(struct, countClassLabelsInDataSet(struct, currentDataSet), currentDataSet.size()); // calculate node entropy
        node.depth = depth; // set node depth

        if(node.entropy == 0) { // if node entropy is 0
            if(currentDataSet.size() != 0) { // and dataset size is not 0
                node.feature = calcMostCommonDatasetLabel(struct ,currentDataSet); // calculate most common dataset label
            } else {
                node.feature = parentMostCommonClassLabel; // set most common label as parent's
            }

            node.attribute = node.feature; // set attribute as feature
            node.mostCommonClassLabel = node.feature; // set most common class label as feature

            return node;
        }

        if(usedFeaturesSet.size() == 0) { // if there is no more features to use in current branch
            node.feature = parentMostCommonClassLabel;
            node.attribute = parentMostCommonClassLabel;
            node.mostCommonClassLabel = parentMostCommonClassLabel;

            return node;
        }

        node.mostCommonClassLabel = calcMostCommonDatasetLabel(struct, currentDataSet); // calc most common class label

        // map contains a feature and data associated with attribute of the feature for current branch
        // this is function is called recursively and data is filtered by the values as function goes recursively
        // e.g. if node before had a feature called "wind" and current branch has wind attribute set to "weak"
        // wind = weak -> there can be no sub-sets where wind != weak
        // e.g. wind (is a feature and is a key) -> weak (attribute of wind, is a sub-key) -> data set (meaning all data where wind = weak)
        Map<String, Map<String, List<List<String>>>> featureAttributeSubSet = new HashMap<>();

        for(Map.Entry<String, List<String>> attributes : struct.featuresAttributes.entrySet()) {

            // skip calculating sub sets of features that are already set as nodes
            if(!usedFeaturesSet.contains(attributes.getKey()))
                continue;

            Map<String, List<List<String>>> attributeSubSet = new HashMap<>();

            for (String attribute : attributes.getValue())
                // put values that match attribute value of feature e.g. attribute.getKey() = wind and attribute = weak
                attributeSubSet.put(attribute, getAttributeSubset(struct, currentDataSet, attribute, attributes.getKey()));

            featureAttributeSubSet.put(attributes.getKey(), attributeSubSet); // put sub set in map
        }

        // calculate best IG
        AbstractMap.SimpleImmutableEntry<String, Double> bestDiscriminativeFeature = calcBestIG(struct,
                featureAttributeSubSet,
                currentDataSet.size(),
                usedFeaturesSet,
                node.entropy);

        usedFeaturesSet.remove(bestDiscriminativeFeature.getKey()); // remove feature from feature set

        // for each attribute value calculate node
        node.feature = bestDiscriminativeFeature.getKey();
        node.ig = bestDiscriminativeFeature.getValue();

        for(String attribute : struct.featuresAttributes.get(node.feature)) {
            Node child = buildDecisionTree(struct, featureAttributeSubSet.get(node.feature).get(attribute), new HashSet<>(usedFeaturesSet), node.mostCommonClassLabel, depth + 1);
            child.attribute = attribute;
            child.parent = node;
            node.children.add(child);
        }
        return node;
    }

    public static AbstractMap.SimpleImmutableEntry<String, Double> calcBestIG(Tree struct, Map<String, Map<String, List<List<String>>>> featureAttributeSubSet, int currentDataSetSize, Set<String> usedFeaturesSet,  double nodeEntropy) {

        Map<String, Double> featureIGMap = new HashMap<>(); // map IG of each feature

        // attribute (sub-key) -> data set (attribute = value)
        for(Map.Entry<String, Map<String, List<List<String>>>> feature : featureAttributeSubSet.entrySet()) {

            if(!usedFeaturesSet.contains(feature.getKey())) // if feature is already used as a node, skip
                continue;

            double ig = nodeEntropy; // starting IG

            // for each attribute in dataset
            for(Map.Entry<String, List<List<String>>> attribute : feature.getValue().entrySet()) {
                double attributeEntropy = entropy(struct,
                        countClassLabelsInDataSet(struct, attribute.getValue()), attribute.getValue().size()); // calculate dataset entropy
                ig -= attributeEntropy * ((double) attribute.getValue().size() / (currentDataSetSize)); // subtract from starting IG
            }

            featureIGMap.put(feature.getKey(), ig); // save IG
        }

        double maxIG = -1;
        String nodeFeature = null;
        for(Map.Entry<String, Double> IGentry : featureIGMap.entrySet()) {
            if(IGentry.getValue() > maxIG) {
                maxIG = IGentry.getValue();
                nodeFeature = IGentry.getKey();
            } else if(IGentry.getValue() == maxIG && nodeFeature.compareTo(IGentry.getKey()) > 0) { // Objects.requireNonNull(nodeFeature).compareTo(IGentry.getKey()
                nodeFeature = IGentry.getKey();
            }
        }

        return new AbstractMap.SimpleImmutableEntry<>(nodeFeature, maxIG);
    }

    // returns an array of each class label occurrence in given dataset labels: [yes, no] -> [ n. of yes, n. of no]
    public static List<Integer> countClassLabelsInDataSet(Tree struct, List<List<String>> currentDataSet) {

        List<Integer> nOfClassLabelCount = new ArrayList<>();
        for(int i = 0; i < struct.classLabels.size(); i++) // initialize all to 0
            nOfClassLabelCount.add(0);

        for(int i = 0; i < struct.classLabels.size(); i++) // count label occurrences
            for (List<String> strings : currentDataSet)
                if (struct.classLabels.get(i).equals(strings.get(struct.classLabelPosition)))
                    nOfClassLabelCount.set(i, nOfClassLabelCount.get(i) + 1);

        return nOfClassLabelCount;
    }

    // get data subset for given attribute value
    public static List<List<String>> getAttributeSubset(Tree struct, List<List<String>> currentDataSet, String attribute, String feature) {

        List<List<String>> subSet = new ArrayList<>(); // list for return value
        for(List<String> entry : currentDataSet) // for each entry
            if(entry.get(struct.featuresIndexMap.get(feature)).equals(attribute)) // get attribute value and compare to wanted attribute
                subSet.add(entry);

        return subSet;
    }

    public static void predict(Tree struct) {

        printBranches(struct.root, struct.depth); // print branches used

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < struct.testData.size(); i++) {
            struct.result.add(makePrediction(struct, struct.root, struct.testData.get(i)));
            sb.append(" " + struct.result.get(i));
        }

        System.out.print("[PREDICTIONS]:");
        System.out.print(sb);
        System.out.println();
    }

    public static String makePrediction(Tree struct, Node node, List<String> entry) {

        if(node.children.size() == 0) {
            return node.feature;
        } else if(node.depth == struct.depth) {
            return node.mostCommonClassLabel;
        } else {
            for(Node child : node.children) {
                if(child.attribute.equals(entry.get(struct.featuresIndexMap.get(node.feature)))) {
                    return makePrediction(struct, child, entry);
                }
            }
        }

        return node.mostCommonClassLabel;
    }

    public static double entropy(Tree struct, List<Integer> nOfClassLabelCount, int currentDataSetSize) {

        double result = 0;

        if(currentDataSetSize == 0) {
            return result;
        }

        for(int n : nOfClassLabelCount) {
            double x = (double) n / (currentDataSetSize);
            result +=  x == 0 ? 0 : -x * (Math.log10(x) * (1 / Math.log10(struct.classLabels.size())));
        }

        return result;
    }

    public static void printBranches(Node node, int depthLimit) {
        System.out.println("[BRANCHES]:");
        // print values for non root nodes
        for(Node child : node.children) {
            printBranchesRecursive(child, depthLimit, "");
        }
    }

    public static void printBranchesRecursive(Node node, int depthLimit, String chain) {
        if(node.depth == depthLimit || node.children.size() == 0) {
            System.out.println(chain + node.depth + ":" + node.parent.feature + "=" + node.attribute + " " + node.mostCommonClassLabel);
            return;
        }

        // for each child
        for(Node child : node.children) {
            // print depth, parent node feature and current node attribute
            printBranchesRecursive(child, depthLimit, chain + node.depth + ":" + node.parent.feature + "=" + node.attribute + " ");
        }
    }

    public static void calcAccuracy(Tree struct) {

        System.out.print("[ACCURACY]: ");
        int correct = 0;
        for(int i = 0; i < struct.testData.size(); i++) {
            if(struct.testData.get(i).get(struct.classLabelPosition).equals(struct.result.get(i))) {
                correct++;
            }
        }

        System.out.printf(Locale.ROOT, "%.5f%n", (double) correct / struct.testData.size());
    }

    public static void confusionMatrix(Tree struct) {
        System.out.println("[CONFUSION_MATRIX]:");

        // real class label
        for(int i = 0; i < struct.classLabels.size(); i++) {
            // predicted class label
            for(int j = 0; j < struct.testClassLabels.size(); j++) {

                int match = 0;

                for(int k = 0; k < struct.testData.size(); k++) {
                    if(struct.testData.get(k).get(struct.classLabelPosition).equals(struct.classLabels.get(i)) && struct.result.get(k).equals(struct.testClassLabels.get(j))) {
                        match++;
                    }
                }

                if(j == struct.classLabels.size() - 1) {
                    System.out.print(match);
                } else {
                    System.out.print(match + " ");
                }
            }

            System.out.println();
        }
    }


    public static String calcMostCommonDatasetLabel(Tree struct, List<List<String>> currentDataSet) {
        List<Integer> nOfOccurrence = new ArrayList<>();

        for(int i = 0; i < struct.classLabels.size(); i++) {
            nOfOccurrence.add(0);
        }

        for(List<String> entry : currentDataSet) {
            for(int i = 0; i < struct.classLabels.size(); i++) {
                if(entry.get(struct.classLabelPosition).equals(struct.classLabels.get(i))) {
                    nOfOccurrence.set(i, nOfOccurrence.get(i) + 1);
                    break;
                }
            }
        }

        int max = -1;
        int index = 0;

        for(int i = 0; i < struct.classLabels.size(); i++) {
            // class labels are ordered, so we want to keep first highest occurrence in case there are 2 matching occurrences
            if(nOfOccurrence.get(i) > max) {
                max = nOfOccurrence.get(i);
                index = i;
            }
        }

        return struct.classLabels.get(index);
    }
}
