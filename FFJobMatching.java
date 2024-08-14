package CS5800_Project;
import java.io.BufferedReader;
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

class FFJobMatching {
    private static final int INF = Integer.MAX_VALUE;
    private int[][] residualGraph;
    private int[] parent;

    // A BFS-based function to find if there is a path from source to sink
    private boolean bfs(int source, int sink) {
        boolean[] visited = new boolean[residualGraph.length];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        while (!queue.isEmpty()) {
            int u = queue.poll();

            for (int v = 0; v < residualGraph.length; v++) {
                if (!visited[v] && residualGraph[u][v] > 0) { // Check if there's an edge with remaining capacity
                    if (v == sink) {
                        parent[v] = u;
                        return true;
                    }
                    queue.add(v);
                    parent[v] = u;
                    visited[v] = true;
                }
            }
        }
        return false; // No augmenting path found
    }

    // Ford-Fulkerson algorithm to find the maximum matching
    public int maxJobMatching(int[][] jobGraph, List<String> nameColumn, List<String> jobIdColumn, List<String> degree, List<String> qualification, String path) {
        int applicants = jobGraph.length;
        int jobs = jobGraph[0].length;

        // Create a super source and super sink
        int source = 0;
        int sink = applicants + jobs + 1;

        // Build the residual graph
        residualGraph = new int[applicants + jobs + 2][applicants + jobs + 2];

        // Connect source to applicant vertices
        for (int i = 1; i <= applicants; i++) {
            residualGraph[source][i] = 1;
        }

        // Connect job vertices to sink
        for (int i = 1; i <= jobs; i++) {
            residualGraph[applicants + i][sink] = 1;
        }

        // Fill the bipartite edges between applicants and jobs
        for (int u = 0; u < applicants; u++) {
            for (int v = 0; v < jobs; v++) {
                if (jobGraph[u][v] == 1) {
                    residualGraph[u + 1][applicants + v + 1] = 1;
                }
            }
        }

        parent = new int[applicants + jobs + 2];
        int maxFlow = 0;

        // Augment the flow while there is a path from source to sink
        while (bfs(source, sink)) {
            int pathFlow = INF;

            // Find the minimum capacity in the augmenting path
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residualGraph[u][v]);
            }

            // Update residual capacities of the edges and reverse edges
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residualGraph[u][v] -= pathFlow;
                residualGraph[v][u] += pathFlow;
            }

            maxFlow += pathFlow;
        }

        // Print the matched jobs
        System.out.println("Matched Jobs:");
        String csvFileout = path+"\\FF_output.csv";
        try(CSVWriter writer = new CSVWriter(new FileWriter(csvFileout))){
            String[] b = {"name","degree","JobId", "Qualification"};
            writer.writeNext(b);
            for (int u = 1; u <= applicants; u++) {
                for (int v = 1; v <= jobs; v++) {
                    if (residualGraph[applicants + v][u] > 0) {
                        System.out.println(nameColumn.get(u-1) + " -> " + degree.get(u-1) + " -> " + jobIdColumn.get(v-1) + " -> " + qualification.get(v-1));
                        String[] a = new String[4];
                        a[0] = nameColumn.get(u-1);
                        a[1] = degree.get(u-1);
                        a[2] = jobIdColumn.get(v-1);
                        a[3] = qualification.get(v-1);
                        // System.out.println(a);
                        writer.writeNext(a);
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        return maxFlow; // The maximum flow is the maximum job matching
    }

    // Method to read the matrix from CSV file
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
        String csvFile = paths[0];
        String outputpath = paths[1];
        int[][] matrix = new int[1000][1000];
        List<String> nameColumn = new ArrayList<>();
        List<String> degree = new ArrayList<>();
        List<String> jobIdColumn = new ArrayList<>();
        List<String> qualification = new ArrayList<>();

        try {
            List<String[]> allRows = readCSV(csvFile);

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
            System.out.println("Name Column: " + nameColumn);
            // System.out.println("Qualification Column: " + degree);
            System.out.println("Job ID Column: " + jobIdColumn);
            System.out.println("qualification: " + qualification);
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
        FFJobMatching ff = new FFJobMatching();

        long startTime = System.nanoTime();
        System.out.println("Maximum Job Matching is " + ff.maxJobMatching(matrix, nameColumn, jobIdColumn, degree, qualification, outputpath));
        long endTime = System.nanoTime();

        System.out.println("Ford's Algorithm Runtime: " + (endTime - startTime) + " nanoseconds");
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