package Graphics.GraphicsObjects;

import android.opengl.GLES20;

import Graphics.MyGLRenderer;

/**
 * Created by Daniel on 06/05/2016.
 */
public class TexQuadShader {

    private final String vertexShaderCode =
            "uniform mat4 matrix;" +
                    "attribute vec4 position;" +
                    "varying vec2 textureCoordinate;"+
                    "void main() {" +
                    "  gl_Position = matrix * position;" +
                    "  textureCoordinate = -vec2(position);"+
                    "}";

    private final String fragmentShaderCode =

            "precision mediump float;" +
                    "uniform sampler2D texture;"+
                    "uniform vec4 color;"+
                    "varying vec2 textureCoordinate;"+
                    "void main() {" +
                    "  gl_FragColor = color * texture2D(texture, textureCoordinate);"+
                    "}";

    private static int shaderHandle = -1;
    public TexQuadShader()
    {
        if(shaderHandle == -1)
        {
            // prepare shaders and OpenGL program
            int vertexShader    = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader  = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            shaderHandle = GLES20.glCreateProgram();
            GLES20.glAttachShader(shaderHandle, vertexShader);
            GLES20.glAttachShader(shaderHandle, fragmentShader);
            GLES20.glLinkProgram(shaderHandle);

            positionHandle = GLES20.glGetAttribLocation(shaderHandle, "position");
            textureHandle = GLES20.glGetUniformLocation(shaderHandle, "texture");
            matrixHandle = GLES20.glGetUniformLocation(shaderHandle, "matrix");
            colorHandle = GLES20.glGetUniformLocation(shaderHandle, "color");

        }
    }

    static int colorHandle;
    static int matrixHandle;
    static int textureHandle;
    static int positionHandle;

    void use(Mesh mesh, float[] matrix, float[] color, int texture)
    {
        GLES20.glUseProgram(shaderHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(textureHandle, 0);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(
                positionHandle, mesh.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                mesh.vertexStride, mesh.vertexBuffer);

        MyGLRenderer.checkGlError("glGetUniformLocation");

        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, mesh.triangles.length,
                GLES20.GL_UNSIGNED_SHORT, mesh.drawListBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}