public class Outcast {
    private WordNet localWordNet;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        localWordNet = wordnet;
    }

    // see test client below
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        long maxDistance = -1;
        String outcast = null;
        for (int i = 0; i < nouns.length; i++) {
            int currentDist = 0;
            for (int j = 0; j < nouns.length; j++) {
                if (i != j) {
                    currentDist += localWordNet.distance(nouns[i], nouns[j]);
                }
            }
            if (currentDist > maxDistance) {
                maxDistance = currentDist;
                outcast = nouns[i];
            }
        }
        return outcast;
    }
}
