public class SAP {
    private Digraph localGraph;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        if (G == null) {
            throw new NullPointerException("Graph is null");
        }
        localGraph = new Digraph(G);
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        final int vertexInGraph = localGraph.V();
        if (v >= vertexInGraph || w >= vertexInGraph) {
            throw new IndexOutOfBoundsException("at least one vertex is not exist");
        }
        if (v == w) {
            return 0;
        }
        BreadthFirstDirectedPaths bfdpFromV = new BreadthFirstDirectedPaths(localGraph, v);
        BreadthFirstDirectedPaths bfdpFromW = new BreadthFirstDirectedPaths(localGraph, w);
        int minCommonDist = -1;
        for (int i = 0; i < vertexInGraph; i++) {
            if (bfdpFromV.hasPathTo(i) && bfdpFromW.hasPathTo(i)) {
                int currentDist = bfdpFromV.distTo(i) + bfdpFromW.distTo(i);
                if (minCommonDist < 0) {
                    minCommonDist = currentDist;
                } else if (minCommonDist > currentDist) {
                    minCommonDist = currentDist;
                }
            }
        }
        return minCommonDist;
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        final int vertexInGraph = localGraph.V();
        if (v >= vertexInGraph || w >= vertexInGraph) {
            throw new IndexOutOfBoundsException("at least one vertex is not exist");
        }
        if (v == w) {
            return v;
        }
        BreadthFirstDirectedPaths bfdpFromV = new BreadthFirstDirectedPaths(localGraph, v);
        BreadthFirstDirectedPaths bfdpFromW = new BreadthFirstDirectedPaths(localGraph, w);
        int minCommonDist = -1, commonAncestor = -1;
        for (int i = 0; i < vertexInGraph; i++) {
            if (bfdpFromV.hasPathTo(i) && bfdpFromW.hasPathTo(i)) {
                int currentDist = bfdpFromV.distTo(i) + bfdpFromW.distTo(i);
                if (minCommonDist < 0) {
                    minCommonDist = currentDist;
                    commonAncestor = i;
                } else if (minCommonDist > currentDist) {
                    minCommonDist = currentDist;
                    commonAncestor = i;
                }
            }
        }
        return commonAncestor;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new NullPointerException("at least one of vertex collections are null");
        }
        int shortestAncestralPath = -1;
        for (int vertexV : v) {
            for (int vertexW : w) {
                int currentPathLength = length(vertexV, vertexW);
                if (currentPathLength >= 0) {
                    if (shortestAncestralPath < 0) {
                        shortestAncestralPath = currentPathLength;
                    } else if (shortestAncestralPath > currentPathLength) {
                        shortestAncestralPath = currentPathLength;
                    }
                }
            }
        }
        return shortestAncestralPath;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new NullPointerException("at least one of vertex collections are null");
        }
        int shortestAncestralPath = -1, commonAncestor = -1;
        for (int vertexV : v) {
            for (int vertexW : w) {
                int currentPathLength = length(vertexV, vertexW);
                if (currentPathLength >= 0) {
                    if (shortestAncestralPath < 0) {
                        shortestAncestralPath = currentPathLength;
                        commonAncestor = ancestor(vertexV, vertexW);
                    } else if (shortestAncestralPath > currentPathLength) {
                        shortestAncestralPath = currentPathLength;
                        commonAncestor = ancestor(vertexV, vertexW);
                    }
                }
            }
        }
        return commonAncestor;
    }
}