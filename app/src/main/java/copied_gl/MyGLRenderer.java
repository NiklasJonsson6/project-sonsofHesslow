/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package copied_gl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.niklas.projectsonsofhesslow.MainActivity;
import com.example.niklas.projectsonsofhesslow.R;
import com.example.niklas.projectsonsofhesslow.SvgReader;

import java.io.File;
import java.io.IOException;

import gl_own.FilledBeizierPath;
import gl_own.Camera;
import gl_own.Geometry.Vector2;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    public static FilledBeizierPath[] beiziers;
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private float mAngle;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(1f, 1f, 1f, 1.0f);

        float[] verts = {
                -0.5f, 0.5f,0f, // top left
                -0.5f,-0.5f,0f, // bottom left
                 0.5f,-0.5f,0f, // bottom right
                 0.5f, 0.5f,0f, // top right
        };

        Vector2[] beizierPoints = new Vector2[]
                {
                    new Vector2(0,0),
                    new Vector2(-1,0),
                    new Vector2(-1,1),
                    new Vector2(0,1),
                    new Vector2(0.5f,1),
                    new Vector2(1,0.5f),
                };
            System.out.println("........... 1");
            File homedir = new File("/../");
            System.out.println("........... 2");
            System.out.println(homedir.getAbsolutePath());
            System.out.println("........... 3");
            //beizier= new FilledBeizierPath(beizierPoints,mMVPMatrix);

            File fileToRead = new File(homedir, "raw/drawing.svg");
            try
            {
                beiziers=SvgReader.read(MainActivity.resources.openRawResource(R.raw.drawing),mMVPMatrix);
            }catch (IOException ex)
            {
                throw new RuntimeException(ex.toString());
            }
    }



    @Override
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Camera cam = Camera.getInstance();
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, cam.X(), cam.Y(), cam.Z(), cam.X(), cam.Y(), 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        for(int i = 0; i<beiziers.length;i++)
        {
            beiziers[i].m.matrix = mMVPMatrix;
            beiziers[i].draw();
        }

        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

    }

    public static int width;
    public static int height;

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        this.width = width;
        this.height = height;

        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 7);
    }

    public static Vector2 ScreentoGLCoords(Vector2 vec)
    {
        float ratio = width/(float)height;
        float x = (vec.x/(width/2)-1);
        float y = -(vec.y/(height/2)-1);

        //from  0,0,width,height to -ratio,-1,+ratio,+1 ??
        return new Vector2(x,y);
    }


    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it:
    *
    * <pre>
    * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
    *
    * If the operation is not successful, the check throws an error.
    *
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
        mAngle = angle;
    }
}
