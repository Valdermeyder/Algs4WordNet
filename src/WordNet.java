import java.util.HashMap;
import java.util.Map;

public class WordNet {
    private Map<String, Integer> synsetsMap;
    private Digraph wordsDigraph;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) {
            throw new NullPointerException("Synsets or hypernyms are null");
        }
        String[] synsetLines = new In(synsets).readAllStrings();
        String[] hypernymsLines = new In(hypernyms).readAllStrings();
        synsetsMap = new HashMap<String, Integer>(synsetLines.length);
        for (int i = 0; i < synsetLines.length; i++) {
            String synsetLine = synsetLines[i];
            String[] splitted = synsetLine.split(",");
            if (splitted.length == 3) {
                synsetsMap.put(splitted[1], Integer.valueOf(splitted[0]));
            }
        }
        wordsDigraph = new Digraph(synsetLines.length);
        for (String hypernymsLine : hypernymsLines) {
            String[] hypernymsArr = hypernymsLine.split(",");
            if (hypernymsArr.length > 1) {
                int rootHypernym = Integer.parseInt(hypernymsArr[0]);
                for (int i = 1; i < hypernymsArr.length; i++) {
                    wordsDigraph.addEdge(rootHypernym - 1, Integer.parseInt(hypernymsArr[i]) - 1);
                }
            }
        }
        DirectedCycle directedCycle = new DirectedCycle(wordsDigraph);
        if (!directedCycle.hasCycle()) {
            throw new IllegalArgumentException("Input files do not represent DAG");
        }
    }

    // do unit testing of this class
    public static void main(String[] args) {
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return synsetsMap.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new NullPointerException("word is null");
        }
        if (synsetsMap.containsKey(word)) {
            return true;
        }
        return false;
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (nounA == null || nounB == null) {
            throw new NullPointerException("nounA or nounB is null");
        }
        Integer vertexA = synsetsMap.get(nounA), vertexB = synsetsMap.get(nounB);
        if (vertexA == null || vertexB == null) {
            throw new IllegalArgumentException(nounA + " or " + nounB + " are not a nouns");
        }
        if (vertexA.equals(vertexB)) {
            return 0;
        }
        return new BreadthFirstDirectedPaths(wordsDigraph, vertexA).distTo(vertexB);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (nounA == null || nounB == null) {
            throw new NullPointerException("nounA or nounB is null");
        }
        Integer vertexA = synsetsMap.get(nounA), vertexB = synsetsMap.get(nounB);
        if (vertexA == null || vertexB == null) {
            throw new IllegalArgumentException(nounA + " or " + nounB + " are not a nouns");
        }
        if (vertexA.equals(vertexB)) {
            return nounA;
        }
        BreadthFirstDirectedPaths bfdpFromA = new BreadthFirstDirectedPaths(wordsDigraph, vertexA);
        BreadthFirstDirectedPaths bfdpFromB = new BreadthFirstDirectedPaths(wordsDigraph, vertexB);
        String commonAncestorNoun = null;
        int minCommonDist = -1;
        for (Map.Entry<String, Integer> synset : synsetsMap.entrySet()) {
            final Integer synsetValue = synset.getValue();
            if (synsetValue != vertexA && synsetValue != vertexB && bfdpFromA.hasPathTo(synsetValue) && bfdpFromB.hasPathTo(synsetValue)) {
                int currentDist = bfdpFromA.distTo(synsetValue) + bfdpFromB.distTo(synsetValue);
                if (minCommonDist < 0) {
                    minCommonDist = currentDist;
                    commonAncestorNoun = synset.getKey();
                } else if (minCommonDist > currentDist) {
                    minCommonDist = currentDist;
                    commonAncestorNoun = synset.getKey();
                }
            }
        }
        return commonAncestorNoun;
    }
}