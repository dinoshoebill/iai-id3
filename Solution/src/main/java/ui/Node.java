package ui;

import java.util.ArrayList;
import java.util.List;

public class Node {

    public Node parent = null;
    public List<Node> children = new ArrayList<>();
    public String feature = null;
    public String attribute = null;
    public double entropy = -1;
    public double ig = -1;
    public int depth = -1;
    public String mostCommonClassLabel = null;

    @Override
    public String toString() {
        return "[feature: " + feature + ", attribute: " + attribute + ", depth: " + depth
                + ", entropy: " + entropy + "]";
    }
}
