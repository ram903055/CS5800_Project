import java.util.*;

class Edge {
    int to, capacity, flow;
    Edge reverse;

    public Edge(int to, int capacity) {
        this.to = to;
        this.capacity = capacity;
    }
}

class Graph {
    List<List<Edge>> adj;
    int V;

    public Graph(int V) {
        this.V = V;
        adj = new ArrayList<>(V);
        for (int i = 0; i < V; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int capacity) {
        Edge e1 = new Edge(to, capacity);
        Edge e2 = new Edge(from, 0);
        e1.reverse = e2;
        e2.reverse = e1;
        adj.get(from).add(e1);
        adj.get(to).add(e2);
    }
}

class FordFulkerson {
    static boolean bfs(Graph graph, int s, int t, int[] parent) {
        boolean[] visited = new boolean[graph.V];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(s);
        visited[s] = true;
        parent[s] = -1;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Edge e : graph.adj.get(u)) {
                if (!visited[e.to] && e.capacity > e.flow) {
                    queue.add(e.to);
                    parent[e.to] = u;
                    visited[e.to] = true;
                }
            }
        }

        return visited[t];
    }

    static int fordFulkerson(Graph graph, int s, int t) {
        int[] parent = new int[graph.V];
        int maxFlow = 0;

        while (bfs(graph, s, t, parent)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int v = t; v != s; v = parent[v]) {
                int u = parent[v];
                for (Edge e : graph.adj.get(u)) {
                    if (e.to == v) {
                        pathFlow = Math.min(pathFlow, e.capacity - e.flow);
                        break;
                    }
                }
            }

            for (int v = t; v != s; v = parent[v]) {
                int u = parent[v];
                for (Edge e : graph.adj.get(u)) {
                    if (e.to == v) {
                        e.flow += pathFlow;
                        e.reverse.flow -= pathFlow;
                        break;
                    }
                }
            }

            maxFlow += pathFlow;
        }

        return maxFlow;
    }
}

class EdmondsKarp {
    static boolean bfs(Graph graph, int s, int t, int[] parent) {
        boolean[] visited = new boolean[graph.V];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(s);
        visited[s] = true;
        parent[s] = -1;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Edge e : graph.adj.get(u)) {
                if (!visited[e.to] && e.capacity > e.flow) {
                    queue.add(e.to);
                    parent[e.to] = u;
                    visited[e.to] = true;
                }
            }
        }

        return visited[t];
    }

    static int edmondsKarp(Graph graph, int s, int t) {
        int[] parent = new int[graph.V];
        int maxFlow = 0;

        while (bfs(graph, s, t, parent)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int v = t; v != s; v = parent[v]) {
                int u = parent[v];
                for (Edge e : graph.adj.get(u)) {
                    if (e.to == v) {
                        pathFlow = Math.min(pathFlow, e.capacity - e.flow);
                        break;
                    }
                }
            }

            for (int v = t; v != s; v = parent[v]) {
                int u = parent[v];
                for (Edge e : graph.adj.get(u)) {
                    if (e.to == v) {
                        e.flow += pathFlow;
                        e.reverse.flow -= pathFlow;
                        break;
                    }
                }
            }

            maxFlow += pathFlow;
        }

        return maxFlow;
    }
}

class Dinic {
    static boolean bfs(Graph graph, int s, int t, int[] level) {
        Arrays.fill(level, -1);
        level[s] = 0;
        Queue<Integer> queue = new LinkedList<>();
        queue.add(s);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Edge e : graph.adj.get(u)) {
                if (level[e.to] < 0 && e.capacity > e.flow) {
                    level[e.to] = level[u] + 1;
                    queue.add(e.to);
                }
            }
        }

        return level[t] >= 0;
    }

    static int dfs(Graph graph, int[] level, int[] start, int u, int t, int flow) {
        if (u == t) return flow;

        for (; start[u] < graph.adj.get(u).size(); start[u]++) {
            Edge e = graph.adj.get(u).get(start[u]);

            if (level[e.to] == level[u] + 1 && e.capacity > e.flow) {
                int currFlow = Math.min(flow, e.capacity - e.flow);
                int tempFlow = dfs(graph, level, start, e.to, t, currFlow);

                if (tempFlow > 0) {
                    e.flow += tempFlow;
                    e.reverse.flow -= tempFlow;
                    return tempFlow;
                }
            }
        }

        return 0;
    }

    static int dinic(Graph graph, int s, int t) {
        int maxFlow = 0;
        int[] level = new int[graph.V];

        while (bfs(graph, s, t, level)) {
            int[] start = new int[graph.V];
            while (true) {
                int flow = dfs(graph, level, start, s, t, Integer.MAX_VALUE);
                if (flow == 0) break;
                maxFlow += flow;
            }
        }

        return maxFlow;
    }
}

public class MaxFlowComparison {
    public static void main(String[] args) {
        int V = 6;
        Graph graph;

        System.out.println("-----------------------------------------------");
        System.out.println("Water Distribution Network Maximum Flow Problem");
        System.out.println("-----------------------------------------------");

        // Ford-Fulkerson
        graph = createGraph(V);
        long startTime = System.nanoTime();
        int maxFlow = FordFulkerson.fordFulkerson(graph, 0, 5);
        long endTime = System.nanoTime();
        System.out.println("Ford-Fulkerson Algorithm:");
        System.out.println("Maximum flow: " + maxFlow);
        System.out.println("Time taken: " + (endTime - startTime) / 1e6 + " ms");
        System.out.println("Space complexity: O(V + E)");
        System.out.println();

        // Edmonds-Karp
        graph = createGraph(V);
        startTime = System.nanoTime();
        maxFlow = EdmondsKarp.edmondsKarp(graph, 0, 5);
        endTime = System.nanoTime();
        System.out.println("Edmonds-Karp Algorithm:");
        System.out.println("Maximum flow: " + maxFlow);
        System.out.println("Time taken: " + (endTime - startTime) / 1e6 + " ms");
        System.out.println("Space complexity: O(V + E)");
        System.out.println();

        // Dinic's
        graph = createGraph(V);
        startTime = System.nanoTime();
        maxFlow = Dinic.dinic(graph, 0, 5);
        endTime = System.nanoTime();
        System.out.println("Dinic's Algorithm:");
        System.out.println("Maximum flow: " + maxFlow);
        System.out.println("Time taken: " + (endTime - startTime) / 1e6 + " ms");
        System.out.println("Space complexity: O(V + E)");
    }

    static Graph createGraph(int V) {
        Graph graph = new Graph(V);
        
        graph.addEdge(0, 1, 10);
        graph.addEdge(0, 2, 10);
        graph.addEdge(1, 2, 2);
        graph.addEdge(1, 3, 4);
        graph.addEdge(1, 4, 8);
        graph.addEdge(2, 4, 9);
        graph.addEdge(3, 5, 10);
        graph.addEdge(4, 3, 6);
        graph.addEdge(4, 5, 10);
        return graph;
    }
}
