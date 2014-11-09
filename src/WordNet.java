import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordNet {
    private Map<String, Integer> nounToVertexMap;
    private Digraph wordsDigraph;
    private SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) {
            throw new NullPointerException("Synsets or hypernyms are null");
        }
        List<String> synsetLines = readAllStringsFromInput(synsets);
        final int synsetSize = synsetLines.size();
        nounToVertexMap = new HashMap<String, Integer>(synsetSize);
        for (String synsetLine : synsetLines) {
            String[] splitted = synsetLine.split(",");
            if (splitted.length == 3) {
                String[] nouns = splitted[1].split(" ");
                for (String noun : nouns) {
                    nounToVertexMap.put(noun, Integer.valueOf(splitted[0]));
                }
            }
        }

        List<String> hypernymsLines = readAllStringsFromInput(hypernyms);
        wordsDigraph = new Digraph(synsetSize);
        for (String hypernymsLine : hypernymsLines) {
            String[] hypernymsArr = hypernymsLine.split(",");
            if (hypernymsArr.length > 1) {
                final int rootNode = Integer.parseInt(hypernymsArr[0]);
                for (int i = 1; i < hypernymsArr.length; i++) {
                    wordsDigraph.addEdge(rootNode, Integer.parseInt(hypernymsArr[i]));
                }
            }
        }
/*        DirectedCycle directedCycle = new DirectedCycle(wordsDigraph);
        if (!directedCycle.hasCycle()) {
            throw new IllegalArgumentException("Input files do not represent DAG");
        }*/
        sap = new SAP(wordsDigraph);
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet wordNet = new WordNet("synsets.txt", "hypernyms.txt");
        System.out.println("Distance between communications and spunk = " + wordNet.distance("communications", "spunk"));
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return nounToVertexMap.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new NullPointerException("word is null");
        }
        return nounToVertexMap.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (nounA == null || nounB == null) {
            throw new NullPointerException("nounA or nounB is null");
        }
        Integer vertexA = nounToVertexMap.get(nounA), vertexB = nounToVertexMap.get(nounB);
        if (vertexA == null || vertexB == null) {
            throw new IllegalArgumentException(nounA + " or " + nounB + " are not a nouns");
        }
        return sap.length(vertexA, vertexB);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (nounA == null || nounB == null) {
            throw new NullPointerException("nounA or nounB is null");
        }
        Integer vertexA = nounToVertexMap.get(nounA), vertexB = nounToVertexMap.get(nounB);
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
        for (Map.Entry<String, Integer> synset : nounToVertexMap.entrySet()) {
            final Integer synsetValue = synset.getValue();
            if (bfdpFromA.hasPathTo(synsetValue) && bfdpFromB.hasPathTo(synsetValue)) {
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

    private List<String> readAllStringsFromInput(String fileName) {
        In in = new In(fileName);
        List<String> readLines = new ArrayList<String>();
        String readString;
        while ((readString = in.readLine()) != null) {
            readLines.add(readString);
        }
        return readLines;
    }
}