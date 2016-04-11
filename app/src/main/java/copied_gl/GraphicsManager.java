package copied_gl;

/**
 * Created by Daniel on 11/04/2016.
 */
public class GraphicsManager {
    public static void setColor(int regionIndex, float[] Color)
    {
        MyGLRenderer.beiziers[regionIndex].mesh.color = Color;
    }
}
