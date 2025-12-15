import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MazePanel extends JPanel {
    private Cell[][] maze;
    private int rows, cols, cellSize;

    public MazePanel(int rows, int cols, int cellSize) {
        this.rows = rows;
        this.cols = cols;
        this.cellSize = cellSize;
        this.setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        this.setBackground(new Color(71, 56, 40)); // Dirt color background
    }

    public void setMaze(Cell[][] maze) {
        this.maze = maze;
        repaint(); // Gambar ulang saat maze berubah
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (maze == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                drawCell(g2d, maze[i][j]);
            }
        }
    }
    
    private void drawCell(Graphics2D g, Cell cell) {
        int x = cell.col * cellSize;
        int y = cell.row * cellSize;
        
        // Draw terrain
        g.setColor(cell.terrain.color);
        g.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        
        // Add texture
        drawTerrainTexture(g, cell, x, y);
        
        // Overlay for visited/path
        if (cell.isPath) {
            g.setColor(new Color(255, 215, 0, 230));
            g.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
            g.setColor(new Color(255, 255, 0));
            g.drawRect(x + 1, y + 1, cellSize - 3, cellSize - 3);
        } else if (cell.isVisited) {
            g.setColor(new Color(173, 216, 230, 180));
            g.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        }
        
        // Draw walls
        g.setColor(new Color(80, 80, 80));
        int wallSize = 3;
        if (cell.topWall) g.fillRect(x, y, cellSize, wallSize);
        if (cell.rightWall) g.fillRect(x + cellSize - wallSize, y, wallSize, cellSize);
        if (cell.bottomWall) g.fillRect(x, y + cellSize - wallSize, cellSize, wallSize);
        if (cell.leftWall) g.fillRect(x, y, wallSize, cellSize);
        
        // Draw Objects
        if (cell.isStart) drawSteve(g, x, y);
        if (cell.isEnd) drawDiamond(g, x, y);
    }
    
    private void drawTerrainTexture(Graphics2D g, Cell cell, int x, int y) {
        Color lighter = cell.terrain.color.brighter();
        Color darker = cell.terrain.color.darker();
        Random rand = new Random(cell.row * 1000 + cell.col);
        for (int i = 0; i < 3; i++) {
            int px = x + rand.nextInt(cellSize - 4) + 2;
            int py = y + rand.nextInt(cellSize - 4) + 2;
            g.setColor(rand.nextBoolean() ? lighter : darker);
            g.fillRect(px, py, 2, 2);
        }
    }
    
    private void drawSteve(Graphics2D g, int x, int y) {
        int offset = 4;
        int size = cellSize - 8;
        
        // Head
        g.setColor(new Color(195, 135, 92));
        g.fillRect(x + offset, y + offset, size, size/2);
        // Hair
        g.setColor(new Color(92, 52, 14));
        g.fillRect(x + offset, y + offset, size, size/5);
        // Eyes
        g.setColor(new Color(78, 193, 227));
        g.fillRect(x + offset + 2, y + offset + 3, 3, 2);
        g.fillRect(x + offset + size - 5, y + offset + 3, 3, 2);
        // Body
        g.setColor(new Color(113, 193, 214));
        g.fillRect(x + offset + 1, y + offset + size/2, size - 2, size/2);
    }
    
    private void drawDiamond(Graphics2D g, int x, int y) {
        int cx = x + cellSize/2;
        int cy = y + cellSize/2;
        int size = 8;
        
        // Glow
        g.setColor(new Color(0, 255, 255, 100));
        int[] xPoints = {cx, cx + size, cx, cx - size};
        int[] yPoints = {cy - size, cy, cy + size, cy};
        g.fillPolygon(xPoints, yPoints, 4);
        
        // Main diamond
        g.setColor(new Color(0, 255, 255));
        size = 6;
        int[] xPoints2 = {cx, cx + size, cx, cx - size};
        int[] yPoints2 = {cy - size, cy, cy + size, cy};
        g.fillPolygon(xPoints2, yPoints2, 4);
        
        // Highlight
        g.setColor(Color.WHITE);
        g.fillRect(cx - 2, cy - 3, 2, 2);
    }
}