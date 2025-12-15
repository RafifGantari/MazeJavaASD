import java.awt.Color;

public enum TerrainType {
    STONE(0, new Color(128, 128, 128)),
    GRASS(1, new Color(124, 176, 70)),
    SAND(5, new Color(237, 201, 175)),
    LAVA(10, new Color(207, 87, 0));
    
    final int weight;
    final Color color;
    
    TerrainType(int weight, Color color) {
        this.weight = weight;
        this.color = color;
    }
}