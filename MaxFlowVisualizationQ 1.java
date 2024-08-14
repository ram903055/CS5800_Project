import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

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

class GraphPanel extends JPanel {
    private Graph graph;
    private String[] vertexNames;
    private static final int VERTEX_RADIUS = 20;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    public GraphPanel(Graph graph, String[] vertexNames) {
        this.graph = graph;
        this.vertexNames = vertexNames;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw edges
        for (int i = 0; i < graph.V; i++) {
            Point p1 = getVertexCenter(i);
            for (Edge e : graph.adj.get(i)) {
                if (e.capacity > 0) {  // Only draw forward edges
                    Point p2 = getVertexCenter(e.to);
                    drawArrow(g2d, p1.x, p1.y, p2.x, p2.y);
                    String label = String.valueOf(e.capacity);
                    g2d.drawString(label, (p1.x + p2.x) / 2, (p1.y + p2.y) / 2 - 5);
                }
            }
        }

        // Draw vertices
        for (int i = 0; i < graph.V; i++) {
            Point p = getVertexCenter(i);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(p.x - VERTEX_RADIUS, p.y - VERTEX_RADIUS, 2 * VERTEX_RADIUS, 2 * VERTEX_RADIUS);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(p.x - VERTEX_RADIUS, p.y - VERTEX_RADIUS, 2 * VERTEX_RADIUS, 2 * VERTEX_RADIUS);
            g2d.drawString(vertexNames[i], p.x - g2d.getFontMetrics().stringWidth(vertexNames[i]) / 2, p.y + 5);
        }
    }

    private Point getVertexCenter(int index) {
        int x = (int) (WIDTH / 2 + WIDTH / 3 * Math.cos(2 * Math.PI * index / graph.V));
        int y = (int) (HEIGHT / 2 + HEIGHT / 3 * Math.sin(2 * Math.PI * index / graph.V));
        return new Point(x, y);
    }

    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 10;
        int dx = (int) (arrowSize * Math.cos(angle - Math.PI / 6));
        int dy = (int) (arrowSize * Math.sin(angle - Math.PI / 6));
        g2d.drawLine(x2, y2, x2 - dx, y2 - dy);
        dx = (int) (arrowSize * Math.cos(angle + Math.PI / 6));
        dy = (int) (arrowSize * Math.sin(angle + Math.PI / 6));
        g2d.drawLine(x2, y2, x2 - dx, y2 - dy);
    }
}

public class MaxFlowVisualizationQ {
    private static final String[] VERTEX_NAMES = {"Source", "A", "B", "C", "D", "Sink"};

    public static void main(String[] args) {
        Graph graph = createGraph();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Graph Visualization");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new GraphPanel(graph, VERTEX_NAMES));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static Graph createGraph() {
        Graph graph = new Graph(6);
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
