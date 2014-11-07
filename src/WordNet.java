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
        In in = new In(synsets);
        List<String> synsetLines = new ArrayList<String>();
        String readString;
        while ((readString = in.readLine()) != null) {
            synsetLines.add(readString);
        }
        in = new In(hypernyms);
        List<String> hypernymsLines = new ArrayList<String>();
        while ((readString = in.readLine()) != null) {
            hypernymsLines.add(readString);
        }
        final int synsetSize = synsetLines.size();
        nounToVertexMap = new HashMap<String, Integer>(synsetSize);
        Map<Integer, String> idToSynsetMap = new HashMap<Integer, String>(synsetSize);
        for (int i = 0; i < synsetSize; i++) {
            String synsetLine = synsetLines.get(i);
            String[] splitted = synsetLine.split(",");
            if (splitted.length == 3) {
                String[] nouns = splitted[1].split(" ");
                for (String noun : nouns) {
                    nounToVertexMap.put(noun, i);
                }
                idToSynsetMap.put(Integer.valueOf(splitted[0]), splitted[1]);
            }
        }
        wordsDigraph = new Digraph(synsetSize);
        for (String hypernymsLine : hypernymsLines) {
            String[] hypernymsArr = hypernymsLine.split(",");
            if (hypernymsArr.length > 1) {
                String rootNoun = idToSynsetMap.get(Integer.parseInt(hypernymsArr[0]));
                if (rootNoun != null) {
                    final int indexOf = rootNoun.indexOf(' ');
                    int rootNounId = nounToVertexMap.get(indexOf == -1 ? rootNoun : rootNoun.substring(0, indexOf));
                    for (int i = 1; i < hypernymsArr.length; i++) {
                        String hypertmedNoun = idToSynsetMap.get(Integer.parseInt(hypernymsArr[i]));
                        if (hypertmedNoun != null) {
                            final int indexOfSpace = hypertmedNoun.indexOf(' ');
                            wordsDigraph.addEdge(rootNounId, nounToVertexMap.get(indexOfSpace == -1 ? hypertmedNoun : hypertmedNoun.substring(0, indexOfSpace)));
                        }
                    }
                }
            }
        }
        DirectedCycle directedCycle = new DirectedCycle(wordsDigraph);
        if (!directedCycle.hasCycle()) {
            throw new IllegalArgumentException("Input files do not represent DAG");
        }
        sap = new SAP(wordsDigraph);
    }

    // do unit testing of this class
    public static void main(String[] args) {
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
            if (!synsetValue.equals(vertexA) && !synsetValue.equals(vertexB) && bfdpFromA.hasPathTo(synsetValue) && bfdpFromB.hasPathTo(synsetValue)) {
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