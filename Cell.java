import java.util.Objects;

public class Cell {
    public int row, col;
    public boolean topWall = true, rightWall = true, bottomWall = true, leftWall = true;
    public boolean isVisited = false, isPath = false, isStart = false, isEnd = false;
    public TerrainType terrain = TerrainType.STONE;
    
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return row == cell.row && col == cell.col;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}