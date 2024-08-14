package CS5800_Project;
import java.util.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import java.util.List;
import java.util.Queue;

class DinicJobMatching {
    private static final int INF = Integer.MAX_VALUE;

    private int vertices;
    private List<Edge>[] adjList;
    private int[] level;

    private static class Edge {
        int to, rev, capacity, flow;

        Edge(int to, int rev, int capacity) {
            this.to = to;
            this.rev = rev;
            this.capacity = capacity;
            this.flow = 0;
        }
    }

    public DinicJobMatching(int vertices) {
        this.vertices = vertices;
        adjList = new ArrayList[vertices];
        for (int i = 0; i < vertices; i++) {
            adjList[i] = new ArrayList<>();
        }
        level = new int[vertices];
    }

    public void addEdge(int u, int v, int capacity) {
        adjList[u].add(new Edge(v, adjList[v].size(), capacity));
        adjList[v].add(new Edge(u, adjList[u].size() - 1, 0));
    }

    private boolean bfs(int source, int sink) {
        Arrays.fill(level, -1);
        level[source] = 0;
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Edge edge : adjList[u]) {
                if (level[edge.to] < 0 && edge.flow < edge.capacity) {
                    level[edge.to] = level[u] + 1;
                    queue.add(edge.to);
                }
            }
        }

        return level[sink] >= 0;
    }

    private int dfs(int u, int sink, int flow, int[] start) {
        if (u == sink) return flow;

        for (; start[u] < adjList[u].size(); start[u]++) {
            Edge edge = adjList[u].get(start[u]);

            if (level[edge.to] == level[u] + 1 && edge.flow < edge.capacity) {
                int currFlow = Math.min(flow, edge.capacity - edge.flow);
                int tempFlow = dfs(edge.to, sink, currFlow, start);

                if (tempFlow > 0) {
                    edge.flow += tempFlow;
                    adjList[edge.to].get(edge.rev).flow -= tempFlow;
                    return tempFlow;
                }
            }
        }

        return 0;
    }

    public int maxJobMatching(int[][] jobGraph, List<String> nameColumn, List<String> jobIdColumn, List<String> degree, List<String> qualification, String path) {
        int applicants = jobGraph.length;
        int jobs = jobGraph[0].length;

        int source = 0;
        int sink = applicants + jobs + 1;

        // Initialize the DinicJobMatching object correctly
        DinicJobMatching dinic = new DinicJobMatching(applicants + jobs + 2);

        for (int i = 1; i <= applicants; i++) {
            dinic.addEdge(source, i, 1);
        }

        for (int i = 1; i <= jobs; i++) {
            dinic.addEdge(applicants + i, sink, 1);
        }

        for (int u = 0; u < applicants; u++) {
            for (int v = 0; v < jobs; v++) {
                if (jobGraph[u][v] == 1) {
                    dinic.addEdge(u + 1, applicants + v + 1, 1);
                }
            }
        }

        int totalFlow = 0;

        while (dinic.bfs(source, sink)) {
            int[] start = new int[dinic.vertices];
            int flow;

            while ((flow = dinic.dfs(source, sink, INF, start)) > 0) {
                totalFlow += flow;
            }
        }

        // Print the matched jobs
        System.out.println("Matched Jobs:");
        String csvFileout = path+"\\Dinic_output.csv";
        try(CSVWriter writer = new CSVWriter(new FileWriter(csvFileout))){
            String[] b = {"name","degree","JobId", "Qualification"};
            writer.writeNext(b);
            for (int u = 1; u <= applicants; u++) {
                for (Edge edge : dinic.adjList[u]) {
                    if (edge.flow > 0 && edge.to != source) {
                        System.out.println(nameColumn.get(u-1) + " -> " + degree.get(u-1) +" -> Job ID: " + jobIdColumn.get(edge.to - applicants-1) + " -> " + qualification.get(edge.to - applicants-1));
                        String[] a = new String[4];
                        a[0] = nameColumn.get(u-1);
                        a[1] = degree.get(u-1);
                        a[2] = jobIdColumn.get(edge.to - applicants-1);
                        a[3] = qualification.get(edge.to - applicants-1);
                        // System.out.println(a);
                        writer.writeNext(a);
                    }

                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        return totalFlow;
    }

    public static String[] openFileAndDirectory() {
        final String[] paths = new String[2]; // 0 - input file path, 1 - output directory path
        JFrame frame = new JFrame("Select Input File and Output Directory");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 200);
        frame.setLayout(new GridLayout(3, 1));

        JButton openFileButton = new JButton("Select Input CSV File");
        JButton openDirButton = new JButton("Select Output Directory");

        frame.add(openFileButton);
        frame.add(openDirButton);

        openFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    paths[0] = selectedFile.getAbsolutePath();
                }
            }
        });

        openDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dirChooser = new JFileChooser();
                dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = dirChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = dirChooser.getSelectedFile();
                    paths[1] = selectedDir.getAbsolutePath();
                }
            }
        });

        frame.setVisible(true);
        // Wait for the user to select the file and directory
        while (paths[0] == null || paths[1] == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        frame.dispose();
        return paths;
    }

    public static void main(String[] args) {
        String[] paths = openFileAndDirectory();
        System.out.println(paths[1]);
        String outputpath = paths[1];
        int[][] matrix = new int[1000][1000];
        List<String> nameColumn = new ArrayList<>();
        List<String> degree = new ArrayList<>();
        List<String> jobIdColumn = new ArrayList<>();
        List<String> qualification = new ArrayList<>();

        try {
            List<String[]> allRows = readCSV(paths[0]);

            // Lists to store each column
            

            // Iterate over each row starting from the second row (index 1)
            for (int i = 1; i < 1001; i++) { // Start from index 1
                String[] row = allRows.get(i);

                if (row == null || row.length == 0 || isEmptyRow(row)) {
                    continue; // Skip empty rows
                }

                nameColumn.add(row[0]);          // Reading 'name' column
                degree.add(row[1]); // Reading 'qualification' column
                jobIdColumn.add(row[2]);         // Reading 'jobid' column
                qualification.add(row[3]);
            }

            // Print the columns
            // System.out.println("Name Column: " + nameColumn);
            // System.out.println("Qualification Column: " + degree);
            // System.out.println("Job ID Column: " + jobIdColumn);
            // System.out.println("qualification: " + qualification);
            System.out.println(nameColumn.size());
            // System.out.println(degree.size());
            // System.out.println(jobIdColumn.size());
            System.out.println(qualification.size());

            for(int i=0;i<999;i++){
                for(int j=0;j<999;j++){
                    if(qualification.get(j).contains(degree.get(i))){
                        matrix[i][j] = 1;
                    }
                    else{
                        matrix[i][j] = 0;
                    }
                    // System.out.println("Qualification: "+qualification.get(j)+" degree: "+degree.get(i));
                    // System.out.println(matrix[i][j]);
                    
                }
            }

            // for (int i = 0; i < matrix.length; i++) {
            //     for (int j = 0; j < matrix[i].length; j++) {
            //         System.out.print(matrix[i][j] + " ");
            //     }
            //     System.out.println();
            // }


        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        DinicJobMatching dinic = new DinicJobMatching(matrix.length + matrix[0].length + 2);

        long startTime = System.nanoTime();
        int maxMatching = dinic.maxJobMatching(matrix,nameColumn,jobIdColumn,degree,qualification,outputpath);
        long endTime = System.nanoTime();

        System.out.println("Maximum Job Matching is " + maxMatching); // Output: 4
        System.out.println("Dinic's Algorithm Runtime: " + (endTime - startTime) + " nanoseconds");
    }

    private static List<String[]> readCSV(String filePath) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            return reader.readAll();
        }
    }
    
    // Helper method to check if a row is empty
    private static boolean isEmptyRow(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
