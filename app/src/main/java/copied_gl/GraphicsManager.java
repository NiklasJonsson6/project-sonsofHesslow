package copied_gl;

/**
 * Created by Daniel on 11/04/2016.
 */
public class GraphicsManager {

    public static void setColor(int regionIndex, float[] Color)
    {
        MyGLRenderer.beiziers[regionIndex].mesh.color = Color;
    }

    public static void setTroops(int regionIndex, int numberOfTroups)
    {
        throw new RuntimeException("not yet implemented");
    }

    public static Integer[] getNeighbours(int regionIndex)
    {
        return MyGLRenderer.beizNeighbors[regionIndex];
    }

    public static int getContinentIndex(int regionIndex)
    {
        throw new RuntimeException("not yet implemented");
    }

}
