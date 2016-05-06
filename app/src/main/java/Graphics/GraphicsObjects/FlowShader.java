package Graphics.GraphicsObjects;

import android.opengl.GLES20;

import Graphics.Geometry.Vector3;
import Graphics.MyGLRenderer;

/**
 * Created by Daniel on 06/05/2016.
 */
public class FlowShader {

    private static final String vertexShaderCode =
            "uniform mat4 matrix;" +
                    "attribute vec4 position;" +
                    "varying vec3 pos;"+
                    "void main() {" +
                    "  gl_Position = matrix * position;" +
                    "pos = vec3(position);"+
                    "}";

    private static final String fragmentShaderCode =
                    "precision mediump float;" +
                    "varying vec3 pos;"+
                    "uniform vec4 color_from;" +
                    "uniform vec4 color_to;" +
                    "uniform vec3 origin;"+
                    "uniform float max_dist_sq;"+
                    "void main() {" +
                        "vec3 sub = origin-pos;"+
                        "float dist_sq = dot(sub,sub);"+
                        "float s = 0.4;"+
                        "float f = smoothstep(max_dist_sq-s,max_dist_sq+s,dist_sq);"+
                        "gl_FragColor = mix(color_from,color_to,f);" +
                    "}";

    private static int flowShader = -1;
    public FlowShader()
    {
        if(flowShader == -1)
        {
            // prepare shaders and OpenGL program
            int vertexShader    = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader  = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            flowShader = GLES20.glCreateProgram();             // create empty OpenGL Program
            GLES20.glAttachShader(flowShader, vertexShader);   // add the vertex shader to program
            System.out.println(GLES20.glGetShaderInfoLog(vertexShader));
            GLES20.glAttachShader(flowShader, fragmentShader); // add the fragment shader to program
            System.out.println(GLES20.glGetShaderInfoLog(fragmentShader));
            GLES20.glLinkProgram(flowShader);                  // create OpenGL program executables
            System.out.println(GLES20.glGetProgramInfoLog(flowShader));
        }
    }

    void use(Mesh mesh, float[] matrix, Vector3 origin, float maxDistance, float[] fromColor,float[] toColor)
    {
        GLES20.glUseProgram(flowShader);

        final int COORDS_PER_VERTEX = 3;
        //position
        final int positionHandle = GLES20.glGetAttribLocation(flowShader, "position");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(
                positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                mesh.vertexStride, mesh.vertexBuffer);

        //color
        final int fromColorHandle = GLES20.glGetUniformLocation(flowShader, "color_from");
        GLES20.glUniform4fv(fromColorHandle, 1, fromColor, 0);

        final int toColorHandle = GLES20.glGetUniformLocation(flowShader, "color_to");
        GLES20.glUniform4fv(toColorHandle, 1, toColor, 0);

        float[] originArr = new float[]{origin.x,origin.y,origin.z};
        final int originHandle = GLES20.glGetUniformLocation(flowShader, "origin");
        GLES20.glUniform3fv(originHandle, 1, originArr, 0);

        final int maxDistHandle = GLES20.glGetUniformLocation(flowShader, "max_dist_sq");
        GLES20.glUniform1f(maxDistHandle, maxDistance * maxDistance);


        //matrix
        final int matrixHandle = GLES20.glGetUniformLocation(flowShader, "matrix");
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
