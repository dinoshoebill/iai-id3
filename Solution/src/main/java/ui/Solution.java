package ui;

import java.util.ArrayList;

public class Solution {

    public static void main(String[] args) {
        try {
            Tree struct = new Tree();

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--train" -> struct.trainingFile = args[++i];
                    case "--test" -> struct.testFile = args[++i];
                    case "--d" -> struct.depth = Integer.parseInt(args[++i]);
                }
            }

            struct.trainingData = Parser.parseCSV(struct.trainingFile);
            struct.testData = Parser.parseCSV(struct.testFile);
            struct.features = Parser.parseFeatures(struct.trainingFile);
            struct.featuresIndexMap = Parser.buildFeaturesIndexMap(struct.features);
            struct.featuresAttributes = Parser.parseFeatureAttributes(struct);
            struct.classLabels = Parser.parseClassLabels(struct);
            struct.testClassLabels = new ArrayList<>(struct.classLabels);
            struct.classLabelPosition = struct.features.size();
            struct.mostCommonClassLabel = ID3.calcMostCommonDatasetLabel(struct, struct.trainingData);

            ID3.fit(struct);
            ID3.predict(struct);
            ID3.calcAccuracy(struct);
            ID3.confusionMatrix(struct);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
