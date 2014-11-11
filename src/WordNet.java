import java.util.*;
import java.util.Stack;

public class WordNet {
    private Map<String, Stack<Integer>> nounToVertexMap;
    private String[] nounsArr;
    private SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) {
            throw new NullPointerException("Synsets or hypernyms are null");
        }
        List<String> synsetLines = readAllStringsFromInput(synsets);
        final int synsetSize = synsetLines.size();
        nounToVertexMap = new HashMap<String, Stack<Integer>>(synsetSize);
        nounsArr = new String[synsetSize];
        for (String synsetLine : synsetLines) {
            String[] splitted = synsetLine.split(",");
            if (splitted.length >= 2) {
                final Integer index = Integer.valueOf(splitted[0]);
                for (String noun : splitted[1].split(" ")) {
                    final Stack<Integer> vertex = nounToVertexMap.get(noun);
                    if (vertex == null) {
                        final Stack<Integer> newVertex = new Stack<Integer>();
                        newVertex.push(index);
                        nounToVertexMap.put(noun, newVertex);
                    } else {
                        vertex.push(index);
                    }
                }
                nounsArr[index] = splitted[1];
            }
        }

        List<String> hypernymsLines = readAllStringsFromInput(hypernyms);
        Digraph wordsDigraph = new Digraph(synsetSize);
        for (String hypernymsLine : hypernymsLines) {
            String[] hypernymsArr = hypernymsLine.split(",");
            if (hypernymsArr.length > 1) {
                final int rootNode = Integer.parseInt(hypernymsArr[0]);
                for (int i = 1; i < hypernymsArr.length; i++) {
                    wordsDigraph.addEdge(rootNode, Integer.parseInt(hypernymsArr[i]));
                }
            }
        }

        int roots = 0;
        for (int i = 0; i < synsetSize; i++) {
            final Bag<Integer> bag = (Bag<Integer>) wordsDigraph.adj(i);
            if (bag.isEmpty()) {
                roots++;
                if (roots > 1) {
                    throw new IllegalArgumentException("Input files do not represent DAG");
                }
            }
        }

        DirectedCycle directedCycle = new DirectedCycle(wordsDigraph);
        if (directedCycle.hasCycle()) {
            throw new IllegalArgumentException("Input files do not represent DAG");
        }

        sap = new SAP(wordsDigraph);
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet wordNet = new WordNet("synsets.txt", "hypernyms.txt");
        System.out.println("Distance between communications and spunk = " + wordNet.distance("Abutilon", "ghostfish"));
        System.out.println("SAP for communications and spunk = " + wordNet.sap("Abutilon", "ghostfish"));
    }

    // returns all WordNet nounsArr
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
        Stack<Integer> vertexA = nounToVertexMap.get(nounA), vertexB = nounToVertexMap.get(nounB);
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
        Stack<Integer> vertexA = nounToVertexMap.get(nounA), vertexB = nounToVertexMap.get(nounB);
        if (vertexA == null || vertexB == null) {
            throw new IllegalArgumentException(nounA + " or " + nounB + " are not a nouns");
        }
        return nounsArr[sap.ancestor(vertexA, vertexB)];
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