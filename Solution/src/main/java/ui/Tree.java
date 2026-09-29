package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tree {

    public Node root = null;
    public String mostCommonClassLabel = null;
    public List<String> features = new ArrayList<>();
    public Map<String, Integer> featuresIndexMap = new HashMap<>();
    public Map<String, List<String>> featuresAttributes = new HashMap<>();
    public List<String> classLabels = new ArrayList<>();
    public List<String> testClassLabels = new ArrayList<>();
    public int classLabelPosition = -1;
    public List<List<String>> trainingData = new ArrayList<>();
    public List<List<String>> testData = new ArrayList<>();
    public List<String> result = new ArrayList<>();
    public String trainingFile;
    public String testFile;
    public int depth = -1;
}
