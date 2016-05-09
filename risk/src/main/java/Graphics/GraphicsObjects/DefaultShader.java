package Graphics.GraphicsObjects;
import android.opengl.GLES20;
import Graphics.MyGLRenderer;

/**
 * Created by Daniel on 06/05/2016.
 */
public class DefaultShader {

    private static final String vertexShaderCode =
            "uniform mat4 matrix;" +
                    "attribute vec4 position;" +
                    "void main() {" +
                    "  gl_Position = matrix * position;" +
                    "}";

    private static final String fragmentShaderCode =
                    "precision mediump float;" +
                    "uniform vec4 color;" +
                    "void main() {" +
                    "  gl_FragColor = color;" +
                    "}";

    private static int defaultShader = -1;
    public DefaultShader()
    {
        if(defaultShader == -1)
        {
            // prepare shaders and OpenGL program
            int vertexShader    = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader  = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            defaultShader= GLES20.glCreateProgram();
            GLES20.glAttachShader(defaultShader, vertexShader);
            GLES20.glAttachShader(defaultShader, fragmentShader);
            GLES20.glLinkProgram(defaultShader);

            positionHandle = GLES20.glGetAttribLocation(defaultShader, "position");
            colorHandle = GLES20.glGetUniformLocation(defaultShader, "color");
            matrixHandle = GLES20.glGetUniformLocation(defaultShader, "matrix");

        }
    }
    static int positionHandle;
    static int colorHandle;
    static int matrixHandle;

    void use(Mesh mesh, float[] matrix, float[] color)
    {
        GLES20.glUseProgram(defaultShader);

        final int COORDS_PER_VERTEX = 3;
        //position
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(
                positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                mesh.vertexStride, mesh.vertexBuffer);

        //color
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        //matrix
        MyGLRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        //actually draw it
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, mesh.triangles.length,
                GLES20.GL_UNSIGNED_SHORT, mesh.drawListBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
