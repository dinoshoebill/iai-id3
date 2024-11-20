package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tree {

    public Node root = null; // starting node
    public String mostCommonClassLabel = null; // most common label in whole dataset
    public List<String> features = new ArrayList<>(); // list of features
    public Map<String, Integer> featuresIndexMap = new HashMap<>(); // index of feature in csv file
    public Map<String, List<String>> featuresAttributes = new HashMap<>(); // list of attributes for each feature
    public List<String> classLabels = new ArrayList<>(); // class labels
    public List<String> testClassLabels = new ArrayList<>(); // class labels
    public int classLabelPosition = -1; // index of class label in csv file
    public List<List<String>> trainingData = new ArrayList<>(); // train samples
    public List<List<String>> testData = new ArrayList<>(); // test samples
    public List<String> result = new ArrayList<>(); // predictions of test samples
    public String trainingFile; // training samples file
    public String testFile; // testing samples file
    public int depth = -1; // default depth
}
