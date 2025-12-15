import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

public class MazeGenerator extends JFrame {

    private static final int ROWS = 30;
    private static final int COLS = 30;
    private static final int CELL_SIZE = 32;
    private static final int DELAY = 15;
    private static final int WALK_DELAY = 100;

    private static final Color BG_COLOR = new Color(135, 206, 235);     // Langit Biru
    private static final Color PANEL_COLOR = new Color(100, 149, 237);  // Panel Biru
    private static final Color BUTTON_COLOR = new Color(205, 92, 92);   // Merah Bata
    
    private static final Color LABEL_TEXT_COLOR = new Color(60, 40, 20); 
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;

    private static final Font PIXEL_FONT = new Font("Monospaced", Font.BOLD, 20);
    private static final Font LEGEND_FONT = new Font("Monospaced", Font.BOLD, 14);

    private Cell[][] maze;
    private MazePanel mazePanel;
    private WeightedGraph graph;
    private Cell start, end;
    private JPanel sidePanel;

    public MazeGenerator() {
        setTitle("Pixel Maze Quest");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 0)); 

        mazePanel = new MazePanel(ROWS, COLS, CELL_SIZE);
        maze = new Cell[ROWS][COLS];
        initializeMaze();
        mazePanel.setMaze(maze);
        mazePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
        add(mazePanel, BorderLayout.CENTER);

        sidePanel = createSidePanel();
        
        JPanel sidePanelContainer = new PixelLandscapePanel();
        sidePanelContainer.setLayout(new BorderLayout());
        sidePanelContainer.setPreferredSize(new Dimension(240, ROWS * CELL_SIZE));
        
        sidePanelContainer.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, Color.BLACK));
        
        sidePanelContainer.add(sidePanel, BorderLayout.CENTER);
        add(sidePanelContainer, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setResizable(false); 
        customizeOptionPane();
    }

    private static class PixelLandscapePanel extends JPanel {
        public PixelLandscapePanel() {
            setBackground(BG_COLOR);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            int w = getWidth();
            int h = getHeight();

            g2d.setColor(new Color(255, 255, 255, 180));
            drawCloud(g2d, 20, 50, 60);
            drawCloud(g2d, 120, 90, 50);
            drawCloud(g2d, 40, 180, 70);
            drawCloud(g2d, 140, 250, 40);

            drawTree(g2d, 40, h - 90, 100, new Color(34, 100, 34)); 
            drawTree(g2d, 190, h - 110, 120, new Color(34, 100, 34));
            drawTree(g2d, 115, h - 80, 140, new Color(34, 139, 34)); 
            
            int groundHeight = 80;
            
            g2d.setColor(new Color(34, 139, 34)); 
            g2d.fillRect(0, h - groundHeight, w, 15);
            for(int i=0; i<w; i+=10) {
                if (i%20==0) g2d.fillRect(i, h - groundHeight - 4, 4, 4);
            }

            g2d.setColor(new Color(92, 64, 51)); 
            g2d.fillRect(0, h - groundHeight + 15, w, groundHeight - 15);

            g2d.setColor(new Color(120, 90, 70)); 
            Random rng = new Random(12345);
            for(int i=0; i<30; i++) {
                int rx = rng.nextInt(Math.max(1, w - 10));
                int ry = h - groundHeight + 20 + rng.nextInt(groundHeight - 30);
                int size = 4 + rng.nextInt(3) * 4;
                g2d.fillRect(rx, ry, size, size);
            }
        }

        private void drawCloud(Graphics2D g, int x, int y, int size) {
            g.fillRect(x, y, size, size/2);
            g.fillRect(x + size/4, y - size/4, size/2, size/2);
            g.fillRect(x - size/4, y + size/8, size/2, size/3);
            g.fillRect(x + size*3/4, y + size/8, size/3, size/3);
        }

        private void drawTree(Graphics2D g, int x, int y, int height, Color leafColor) {
            int trunkWidth = Math.max(12, height / 6);
            int trunkHeight = height / 2;
            g.setColor(new Color(101, 67, 33));
            g.fillRect(x - trunkWidth/2, y - trunkHeight, trunkWidth, trunkHeight + 20);
            g.setColor(leafColor);
            int crownSize = height / 2;
            g.fillRect(x - crownSize, y - trunkHeight - crownSize/3, crownSize*2, crownSize/2);
            g.fillRect(x - crownSize*3/4, y - trunkHeight - crownSize*2/3, crownSize*3/2, crownSize/2);
            g.fillRect(x - crownSize/2, y - trunkHeight - crownSize, crownSize, crownSize/2);
        }
    }

    private void customizeOptionPane() {
        UIManager.put("OptionPane.background", PANEL_COLOR);
        UIManager.put("Panel.background", PANEL_COLOR);
        UIManager.put("OptionPane.messageFont", PIXEL_FONT);
        UIManager.put("OptionPane.buttonFont", PIXEL_FONT);
        UIManager.put("OptionPane.messageForeground", Color.WHITE); // Tetap putih untuk dialog biru tua
        
        UIManager.put("Button.background", BUTTON_COLOR);
        UIManager.put("Button.foreground", BUTTON_TEXT_COLOR);
        UIManager.put("Button.font", PIXEL_FONT);
        UIManager.put("Button.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 3),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                )
        ));
        UIManager.put("Button.select", BUTTON_COLOR); 
        UIManager.put("Button.focus", new Color(0,0,0,0));
    }

    private JPanel createSidePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        JLabel legendTitle = new JLabel("TERRAIN", SwingConstants.CENTER);
        legendTitle.setFont(PIXEL_FONT);
        legendTitle.setForeground(LABEL_TEXT_COLOR);
        gbc.gridy++; panel.add(legendTitle, gbc);

        gbc.gridy++; panel.add(createLegendItem("STONE (0)", TerrainType.STONE.color), gbc);
        gbc.gridy++; panel.add(createLegendItem("GRASS (1)", TerrainType.GRASS.color), gbc);
        gbc.gridy++; panel.add(createLegendItem("SAND (5)", TerrainType.SAND.color), gbc);
        gbc.gridy++; panel.add(createLegendItem("LAVA (10)", TerrainType.LAVA.color), gbc);

        gbc.gridy++; gbc.insets = new Insets(30, 0, 8, 0);
        panel.add(new JLabel(" "), gbc);
        gbc.insets = new Insets(8, 0, 8, 0);

        JLabel controlTitle = new JLabel("ACTIONS", SwingConstants.CENTER);
        controlTitle.setFont(PIXEL_FONT);
        controlTitle.setForeground(LABEL_TEXT_COLOR);
        gbc.gridy++; panel.add(controlTitle, gbc);

        JButton generateBtn = createPixelButton("NEW MAZE");
        JButton bfsBtn = createPixelButton("BFS RUN");
        JButton dfsBtn = createPixelButton("DFS RUN");
        JButton dijkstraBtn = createPixelButton("DIJKSTRA");
        JButton astarBtn = createPixelButton("A* PATH");
        JButton resetBtn = createPixelButton("RESET");
        resetBtn.setBackground(new Color(70, 130, 180));

        generateBtn.addActionListener(e -> generateMaze());
        bfsBtn.addActionListener(e -> solveBFS());
        dfsBtn.addActionListener(e -> solveDFS());
        dijkstraBtn.addActionListener(e -> solveDijkstra());
        astarBtn.addActionListener(e -> solveAStar());
        resetBtn.addActionListener(e -> resetMaze());

        gbc.gridy++; panel.add(generateBtn, gbc);
        gbc.gridy++; gbc.insets = new Insets(25, 0, 8, 0); panel.add(bfsBtn, gbc); gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridy++; panel.add(dfsBtn, gbc);
        gbc.gridy++; panel.add(dijkstraBtn, gbc);
        gbc.gridy++; panel.add(astarBtn, gbc);
        gbc.gridy++; gbc.insets = new Insets(25, 0, 8, 0); panel.add(resetBtn, gbc);

        return panel;
    }

    private JButton createPixelButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(PIXEL_FONT);
        btn.setBackground(BUTTON_COLOR);
        btn.setForeground(BUTTON_TEXT_COLOR);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.setUI(new BasicButtonUI() {
            @Override
            protected void paintButtonPressed(Graphics g, AbstractButton b) {
                paint(g, b);
            }
        });

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 200, 200), 3),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                )
        ));
        return btn;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        item.setOpaque(false); 
        item.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JPanel colorBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(color);
                g.fillRect(0,0, getWidth(), getHeight());
                Random rng = new Random(text.hashCode());
                if (color.equals(TerrainType.STONE.color)) {
                     g.setColor(new Color(80, 80, 80)); for(int i=0; i<4; i++) g.fillRect(rng.nextInt(getWidth()), rng.nextInt(getHeight()), 2, 2);
                } else if (color.equals(TerrainType.GRASS.color)) {
                     g.setColor(new Color(45, 160, 45)); for(int i=0; i<5; i++) g.fillRect(rng.nextInt(getWidth()), rng.nextInt(getHeight()), 2, 3);
                } else if (color.equals(TerrainType.SAND.color)) {
                     g.setColor(new Color(180, 130, 20)); for(int i=0; i<8; i++) g.fillRect(rng.nextInt(getWidth()), rng.nextInt(getHeight()), 1, 1);
                } else if (color.equals(TerrainType.LAVA.color)) {
                     g.setColor(new Color(255, 140, 0)); for(int i=0; i<2; i++) g.fillRect(rng.nextInt(getWidth()), rng.nextInt(getHeight()), 4, 4);
                }
            }
        };

        colorBox.setPreferredSize(new Dimension(30, 30));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        item.add(colorBox);

        JLabel label = new JLabel(text);
        label.setForeground(LABEL_TEXT_COLOR);
        label.setFont(LEGEND_FONT);
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
            mazePanel.setMaze(maze);
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
                mazePanel.repaint();
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
                mazePanel.repaint();
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
                mazePanel.repaint();
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
                mazePanel.repaint();
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
            mazePanel.repaint();
            sleep(DELAY);
        }

        for (Cell cell : path) {
            mazePanel.setPlayerPosition(cell.row, cell.col);
            sleep(WALK_DELAY);
        }
        
        showPathCost(totalCost);
    }

    private void showPathCost(int cost) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(PANEL_COLOR);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

            JPanel topDecor = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            topDecor.setOpaque(false);
            for(int i=0; i<5; i++) topDecor.add(new JLabel(new PixelDecorationIcon(20, 20, PixelDecorationIcon.Type.STAR)));
            panel.add(topDecor, BorderLayout.NORTH);

            JPanel centerContent = new JPanel(new BorderLayout(10, 0));
            centerContent.setOpaque(false);
            JLabel mainIconLabel = new JLabel(new PixelDecorationIcon(50, 50, PixelDecorationIcon.Type.DIAMOND));
            centerContent.add(mainIconLabel, BorderLayout.WEST);

            JLabel messageLabel = new JLabel("<html><center>QUEST COMPLETE!<br><br>TOTAL COST: <font color='yellow'>" + cost + "</font></center></html>", SwingConstants.CENTER);
            messageLabel.setFont(PIXEL_FONT.deriveFont(20f));
            messageLabel.setForeground(Color.WHITE); // Teks dialog tetap putih (kontras dengan biru panel)
            centerContent.add(messageLabel, BorderLayout.CENTER);
            panel.add(centerContent, BorderLayout.CENTER);

            JPanel bottomDecor = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            bottomDecor.setOpaque(false);
            for(int i=0; i<5; i++) bottomDecor.add(new JLabel(new PixelDecorationIcon(20, 20, PixelDecorationIcon.Type.STAR)));
            panel.add(bottomDecor, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(this, 
                panel, 
                "VICTORY!", 
                JOptionPane.PLAIN_MESSAGE);
        });
    }

    private void resetMaze() {
        initializeMaze();
        graph = null;
        start = null;
        end = null;
        mazePanel.setMaze(maze);
    }

    private void resetSolution() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                maze[i][j].isVisited = false;
                maze[i][j].isPath = false;
            }
        }
        mazePanel.repaint();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MazeGenerator().setVisible(true);
        });
    }

    private static class PixelDecorationIcon implements Icon {
        public enum Type { DIAMOND, STAR }
        private int width, height;
        private Type type;

        public PixelDecorationIcon(int w, int h, Type type) { 
            this.width = w; this.height = h; this.type = type;
        }
        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            int size = Math.min(width, height);

            if (type == Type.DIAMOND) {
                g2d.setColor(new Color(0, 255, 255));
                g2d.fillRect(x + size/2 - size/4, y, size/2, size/2);
                g2d.fillRect(x, y + size/2 - size/4, size, size/2);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(x + size/2 - size/8, y + size/8, size/4, size/4);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(Math.max(2, size/15)));
                g2d.drawRect(x, y + size/2 - size/4, size, size/2);
                g2d.drawRect(x + size/2 - size/4, y, size/2, size/2);
            } else if (type == Type.STAR) {
                g2d.setColor(Color.YELLOW);
                int s = size/4;
                g2d.fillRect(x + s, y, s*2, s*4); 
                g2d.fillRect(x, y + s, s*4, s*2); 
                g2d.setColor(new Color(255, 215, 0)); 
                g2d.fillRect(x + s, y + s, s*2, s*2);
            }
        }
    }
}