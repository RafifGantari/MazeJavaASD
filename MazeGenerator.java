import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MazeGenerator extends JFrame {
    private static final int ROWS = 30;
    private static final int COLS = 30;
    private static final int CELL_SIZE = 22;
    private static final int DELAY = 25;
    
    private Cell[][] maze;
    private MazePanel panel;
    private WeightedGraph graph;
    private Cell start, end;
    
    public MazeGenerator() {
        setTitle("Minecraft Maze Explorer - Find the Diamond!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(139, 90, 43));
        
        // Inisialisasi Panel
        panel = new MazePanel(ROWS, COLS, CELL_SIZE);
        
        maze = new Cell[ROWS][COLS];
        initializeMaze();
        panel.setMaze(maze); // Kirim data maze ke panel
        
        add(panel, BorderLayout.CENTER);
        
        // Setup UI Controls
        setupControlPanel();
        add(createLegend(), BorderLayout.NORTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(2, 1));
        controlPanel.setBackground(new Color(139, 90, 43));
        
        JPanel btnPanel1 = new JPanel();
        btnPanel1.setBackground(new Color(139, 90, 43));
        JPanel btnPanel2 = new JPanel();
        btnPanel2.setBackground(new Color(139, 90, 43));
        
        JButton generateBtn = createMinecraftButton("Generate Cave");
        JButton bfsBtn = createMinecraftButton("BFS");
        JButton dfsBtn = createMinecraftButton("DFS");
        JButton dijkstraBtn = createMinecraftButton("Dijkstra");
        JButton astarBtn = createMinecraftButton("A*");
        JButton resetBtn = createMinecraftButton("Reset");
        
        generateBtn.addActionListener(e -> generateMaze());
        bfsBtn.addActionListener(e -> solveBFS());
        dfsBtn.addActionListener(e -> solveDFS());
        dijkstraBtn.addActionListener(e -> solveDijkstra());
        astarBtn.addActionListener(e -> solveAStar());
        resetBtn.addActionListener(e -> resetMaze());
        
        btnPanel1.add(generateBtn);
        btnPanel1.add(bfsBtn);
        btnPanel1.add(dfsBtn);
        btnPanel2.add(dijkstraBtn);
        btnPanel2.add(astarBtn);
        btnPanel2.add(resetBtn);
        
        controlPanel.add(btnPanel1);
        controlPanel.add(btnPanel2);
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private JButton createMinecraftButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Courier New", Font.BOLD, 12));
        btn.setBackground(new Color(106, 106, 106));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(85, 85, 85), 2),
            BorderFactory.createLineBorder(new Color(136, 136, 136), 2)
        ));
        return btn;
    }
    
    private JPanel createLegend() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(new Color(139, 90, 43));
        JLabel title = new JLabel("Terrain Types: ");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Courier New", Font.BOLD, 12));
        panel.add(title);
        panel.add(createLegendItem("Stone (0)", new Color(128, 128, 128)));
        panel.add(createLegendItem("Grass (1)", new Color(124, 176, 70)));
        panel.add(createLegendItem("Sand (5)", new Color(237, 201, 175)));
        panel.add(createLegendItem("Lava (10)", new Color(207, 87, 0)));
        return panel;
    }
    
    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setBackground(new Color(139, 90, 43));
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        item.add(colorBox);
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Courier New", Font.PLAIN, 11));
        item.add(label);
        return item;
    }
    
    private void initializeMaze() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                maze[i][j] = new Cell(i, j);
            }
        }
    }
    
    private void generateMaze() {
        resetMaze();
        new Thread(() -> {
            primAlgorithmWithMorePaths();
            assignTerrainTypes();
            graph = new WeightedGraph(maze, ROWS, COLS);
            start = maze[0][0];
            end = maze[ROWS-1][COLS-1];
            start.isStart = true;
            end.isEnd = true;
            panel.setMaze(maze); // Refresh panel data
            panel.repaint();
        }).start();
    }
    
    private void assignTerrainTypes() {
        Random rand = new Random();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int r = rand.nextInt(100);
                if (r < 45) maze[i][j].terrain = TerrainType.STONE;
                else if (r < 65) maze[i][j].terrain = TerrainType.GRASS;
                else if (r < 82) maze[i][j].terrain = TerrainType.SAND;
                else maze[i][j].terrain = TerrainType.LAVA;
            }
        }
        maze[0][0].terrain = TerrainType.STONE;
        maze[ROWS-1][COLS-1].terrain = TerrainType.STONE;
    }
    
    private void primAlgorithmWithMorePaths() {
        Random rand = new Random();
        List<Wall> walls = new ArrayList<>();
        Set<Cell> visited = new HashSet<>();
        
        Cell current = maze[rand.nextInt(ROWS)][rand.nextInt(COLS)];
        visited.add(current);
        addWalls(current, walls);
        
        while (!walls.isEmpty()) {
            Wall wall = walls.remove(rand.nextInt(walls.size()));
            Cell cell1 = wall.cell1;
            Cell cell2 = wall.cell2;
            
            if (visited.contains(cell1) != visited.contains(cell2)) {
                removeWall(cell1, cell2);
                Cell unvisited = visited.contains(cell1) ? cell2 : cell1;
                visited.add(unvisited);
                addWalls(unvisited, walls);
            }
        }
        
        // Remove additional walls
        List<Wall> allWalls = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Cell c = maze[i][j];
                if (c.rightWall && j < COLS - 1) allWalls.add(new Wall(c, maze[i][j+1]));
                if (c.bottomWall && i < ROWS - 1) allWalls.add(new Wall(c, maze[i+1][j]));
            }
        }
        int wallsToRemove = (int) (allWalls.size() * 0.3);
        for (int i = 0; i < wallsToRemove && !allWalls.isEmpty(); i++) {
            Wall wall = allWalls.remove(rand.nextInt(allWalls.size()));
            removeWall(wall.cell1, wall.cell2);
        }
    }
    
    private void addWalls(Cell cell, List<Wall> walls) {
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for (int[] dir : dirs) {
            int nr = cell.row + dir[0];
            int nc = cell.col + dir[1];
            if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS) {
                walls.add(new Wall(cell, maze[nr][nc]));
            }
        }
    }
    
    private void removeWall(Cell c1, Cell c2) {
        if (c1.row == c2.row) {
            if (c1.col < c2.col) { c1.rightWall = false; c2.leftWall = false; }
            else { c1.leftWall = false; c2.rightWall = false; }
        } else {
            if (c1.row < c2.row) { c1.bottomWall = false; c2.topWall = false; }
            else { c1.topWall = false; c2.bottomWall = false; }
        }
    }
    
    // --- Solvers ---
    
    private void solveBFS() {
        if (graph == null) return;
        resetSolution();
        new Thread(() -> {
            Queue<Cell> queue = new LinkedList<>();
            Map<Cell, Cell> parent = new HashMap<>();
            Set<Cell> visited = new HashSet<>();
            
            queue.offer(start);
            visited.add(start);
            
            while (!queue.isEmpty()) {
                Cell current = queue.poll();
                current.isVisited = true;
                panel.repaint();
                sleep(DELAY);
                
                if (current == end) {
                    tracePath(parent, end);
                    return;
                }
                
                for (Cell neighbor : graph.getNeighbors(current)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parent.put(neighbor, current);
                        queue.offer(neighbor);
                    }
                }
            }
        }).start();
    }
    
    private void solveDFS() {
        if (graph == null) return;
        resetSolution();
        new Thread(() -> {
            Stack<Cell> stack = new Stack<>();
            Map<Cell, Cell> parent = new HashMap<>();
            Set<Cell> visited = new HashSet<>();
            
            stack.push(start);
            visited.add(start);
            
            while (!stack.isEmpty()) {
                Cell current = stack.pop();
                current.isVisited = true;
                panel.repaint();
                sleep(DELAY);
                
                if (current == end) {
                    tracePath(parent, end);
                    return;
                }
                
                for (Cell neighbor : graph.getNeighbors(current)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parent.put(neighbor, current);
                        stack.push(neighbor);
                    }
                }
            }
        }).start();
    }
    
    private void solveDijkstra() {
        if (graph == null) return;
        resetSolution();
        new Thread(() -> {
            PriorityQueue<Node> pq = new PriorityQueue<>();
            Map<Cell, Integer> dist = new HashMap<>();
            Map<Cell, Cell> parent = new HashMap<>();
            Set<Cell> visited = new HashSet<>();
            
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) dist.put(maze[i][j], Integer.MAX_VALUE);
            }
            
            dist.put(start, 0);
            pq.offer(new Node(start, 0));
            
            while (!pq.isEmpty()) {
                Node node = pq.poll();
                Cell current = node.cell;
                
                if (visited.contains(current)) continue;
                visited.add(current);
                current.isVisited = true;
                panel.repaint();
                sleep(DELAY);
                
                if (current == end) {
                    tracePath(parent, end);
                    return;
                }
                
                for (Cell neighbor : graph.getNeighbors(current)) {
                    if (!visited.contains(neighbor)) {
                        int newDist = dist.get(current) + neighbor.terrain.weight;
                        if (newDist < dist.get(neighbor)) {
                            dist.put(neighbor, newDist);
                            parent.put(neighbor, current);
                            pq.offer(new Node(neighbor, newDist));
                        }
                    }
                }
            }
        }).start();
    }
    
    private void solveAStar() {
        if (graph == null) return;
        resetSolution();
        new Thread(() -> {
            PriorityQueue<Node> pq = new PriorityQueue<>();
            Map<Cell, Integer> gScore = new HashMap<>();
            Map<Cell, Cell> parent = new HashMap<>();
            Set<Cell> visited = new HashSet<>();
            
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) gScore.put(maze[i][j], Integer.MAX_VALUE);
            }
            
            gScore.put(start, 0);
            int fScore = heuristic(start, end);
            pq.offer(new Node(start, fScore));
            
            while (!pq.isEmpty()) {
                Node node = pq.poll();
                Cell current = node.cell;
                
                if (visited.contains(current)) continue;
                visited.add(current);
                current.isVisited = true;
                panel.repaint();
                sleep(DELAY);
                
                if (current == end) {
                    tracePath(parent, end);
                    return;
                }
                
                for (Cell neighbor : graph.getNeighbors(current)) {
                    if (!visited.contains(neighbor)) {
                        int tentativeG = gScore.get(current) + neighbor.terrain.weight;
                        if (tentativeG < gScore.get(neighbor)) {
                            gScore.put(neighbor, tentativeG);
                            parent.put(neighbor, current);
                            int f = tentativeG + heuristic(neighbor, end);
                            pq.offer(new Node(neighbor, f));
                        }
                    }
                }
            }
        }).start();
    }
    
    private int heuristic(Cell a, Cell b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }
    
    private void tracePath(Map<Cell, Cell> parent, Cell end) {
        List<Cell> path = new ArrayList<>();
        int totalCost = 0;
        Cell current = end;
        
        while (current != null) {
            path.add(current);
            if (current != start) totalCost += current.terrain.weight;
            current = parent.get(current);
        }
        Collections.reverse(path);
        
        for (Cell cell : path) {
            cell.isPath = true;
            panel.repaint();
            sleep(DELAY);
        }
        showPathCost(totalCost);
    }
    
    private void showPathCost(int cost) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                "Total Weight Optimal Path: " + cost + "\n" +
                "Diamond Found! ⛏", 
                "🎮 Success! 🎮", 
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    private void resetMaze() {
        initializeMaze();
        graph = null;
        start = null;
        end = null;
        panel.setMaze(maze);
        panel.repaint();
    }
    
    private void resetSolution() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                maze[i][j].isVisited = false;
                maze[i][j].isPath = false;
            }
        }
        panel.repaint();
    }
    
    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MazeGenerator().setVisible(true);
        });
    }
}