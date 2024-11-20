package ui;

public class Solution {

    public static void main(String[] args) {

        try {

            Tree struct = new Tree();

            int i = 0;
            do {
                switch (args[i]) {
                    case "--train" -> struct.trainingFile = args[++i];
                    case "--test" -> struct.testFile = args[++i];
                    case "--d" -> struct.depth = Integer.parseInt(args[++i]);
                }

                i++;
            } while (i < args.length);
            
            struct.trainingData = Parser.parseCSV(struct.trainingFile);
            struct.testData = Parser.parseCSV(struct.testFile);
            struct.featuresIndexMap = Parser.parseFeaturesMap(struct.trainingFile);
            struct.features = Parser.parseFeatures(struct.trainingFile);
            struct.featuresAttributes = Parser.parseFeatureAttributes(struct);
            struct.classLabels = Parser.parseClassLabels(struct);
            struct.testClassLabels = Parser.parseClassLabels(struct);
            struct.classLabelPosition = struct.features.size();
            struct.mostCommonClassLabel = ID3.calcMostCommonDatasetLabel(struct, struct.trainingData);

            ID3.fit(struct); // train on samples
            ID3.predict(struct); // make prediction on test samples
            ID3.calcAccuracy(struct); // calculate prediction accuracy
            ID3.confusionMatrix(struct); // result confusion matrix
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}