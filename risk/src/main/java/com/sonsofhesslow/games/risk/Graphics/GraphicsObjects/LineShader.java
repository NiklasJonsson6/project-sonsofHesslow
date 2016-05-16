package com.sonsofhesslow.games.risk.graphics.GraphicsObjects;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

import com.sonsofhesslow.games.risk.graphics.utils.ShaderUtils;

/**
 * Created by Daniel on 06/05/2016.
 */
public class LineShader {

    private static final String vertexShaderCode =
            "uniform mat4 matrix;" +
                    "attribute vec4 position;" +
                    "attribute float side;" +
                    "varying float fside;"+
                    "void main() {" +
                    "  gl_Position = matrix * position;" +
                    " fside = side;"+
                    "}";

    private static final String fragmentShaderCode =
                    "precision mediump float;" +
                    "uniform vec4 color;" +
                    "varying float fside;"+
                    "void main() {" +
                    "float f=0.4;"+
                    "gl_FragColor = color * (smoothstep(0.0,f,fside)*((smoothstep(0.0,f,1.0-fside))));" +
                    "}";


    private static int shaderID = -1;
    public LineShader()
    {
        if(shaderID == -1)
        {
            // prepare shaders and OpenGL program
            int vertexShader    = ShaderUtils.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader  = ShaderUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            shaderID = GLES20.glCreateProgram();             // create empty OpenGL Program
            GLES20.glAttachShader(shaderID, vertexShader);   // add the vertex shader to program
            //System.out.println(GLES20.glGetShaderInfoLog(vertexShader));
            GLES20.glAttachShader(shaderID, fragmentShader); // add the fragment shader to program
            //System.out.println(GLES20.glGetShaderInfoLog(fragmentShader));
            GLES20.glLinkProgram(shaderID);                  // create OpenGL program executables
            //System.out.println(GLES20.glGetProgramInfoLog(shaderID));
        }
    }

    void use(Mesh mesh, float[] matrix, float[] color, FloatBuffer lineSide)
    {
        GLES20.glUseProgram(shaderID);

        final int COORDS_PER_VERTEX = 3;
        //position
        final int positionHandle = GLES20.glGetAttribLocation(shaderID, "position");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(
                positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                mesh.vertexStride, mesh.vertexBuffer);
        //side
        final int sideHandle = GLES20.glGetAttribLocation(shaderID, "side");
        GLES20.glEnableVertexAttribArray(sideHandle);
        GLES20.glVertexAttribPointer(
                sideHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                4, lineSide);

        //color
        final int colorHandle = GLES20.glGetUniformLocation(shaderID, "color");
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        //matrix
        final int matrixHandle = GLES20.glGetUniformLocation(shaderID, "matrix");
        ShaderUtils.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0);
        ShaderUtils.checkGlError("glUniformMatrix4fv");

        //actually draw it
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, mesh.triangles.length,
                GLES20.GL_UNSIGNED_SHORT, mesh.drawListBuffer);

        GLES20.glDisableVertexAttribArray(sideHandle);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}

