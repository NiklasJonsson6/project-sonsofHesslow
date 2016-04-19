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
package Graphics;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.niklas.projectsonsofhesslow.MainActivity;
import com.example.niklas.projectsonsofhesslow.R;

import Graphics.GraphicsObjects.Mesh;
import Graphics.GraphicsObjects.SvgReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Graphics.GraphicsObjects.FilledBeizierPath;
import Graphics.GraphicsObjects.Camera;
import Graphics.GraphicsObjects.GLObject;
import Graphics.Geometry.Vector2;
import Graphics.Geometry.Vector3;

import java.util.concurrent.*;

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

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    public static final float[] mMVPMatrix = new float[16];
    private static final float[] mProjectionMatrix = new float[16];
    private static final float[] mViewMatrix = new float[16];
    private float mAngle;

    static List<GLObject> gameObjects = new ArrayList<>();
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(1f, 1f, 1f, 1.0f);

        try
        {
            List<SvgReader.SVG_ReturnValue> tmp = SvgReader.read(MainActivity.resources.openRawResource(R.raw.drawing));
            GraphicsManager.beiziers = new FilledBeizierPath[tmp.size()];
            GraphicsManager.beizNeighbors = new Integer[tmp.size()][];
            GraphicsManager.beizContinents = new Integer[tmp.size()];
            int c = 0;
            for(SvgReader.SVG_ReturnValue ret : tmp)
            {
                GraphicsManager.beiziers[c] = ret.path;
                GraphicsManager.beizNeighbors[c] = ret.neighbors;
                GraphicsManager.beizContinents[c] = ret.continent_id;
                ++c;
            }

        } catch (IOException ex)
        {
            throw new RuntimeException(ex.toString());
        }
    }

    static ConcurrentLinkedQueue<GLObject> objectsToBeAdded = new ConcurrentLinkedQueue<>();
    public static void delayed_init(GLObject m)
    {
        objectsToBeAdded.add(m);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        for(GLObject go : objectsToBeAdded)
        {
            for(Mesh m : go.getMeshes()) m.init();
            gameObjects.add(go);
        }
        objectsToBeAdded.clear();
        float[] scratch = new float[16];

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Camera cam = Camera.getInstance();
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, cam.pos.x, cam.pos.y, cam.pos.z, cam.lookAt.x, cam.lookAt.y, cam.lookAt.z, cam.up.x, cam.up.y, cam.up.z);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        for(GLObject go : gameObjects)
        {
            if(go.isActive)
                go.draw(mMVPMatrix);
        }
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
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 20);
    }

    public static Vector3 ScreenToGl(Vector2 point)
    {
        return new Vector3((point.x) * 2.0f / width, ((point.y) * 2.0f / height),0);
    }

    public static Vector2 ScreenToWorldCoords(Vector2 point,float z_out)
    {
        float[] transformMatrix = new float[16];
        Matrix.multiplyMM(transformMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        float[] invTransformMatrix = new float[16];
        Matrix.invertM(invTransformMatrix, 0, transformMatrix, 0);

        if(invTransformMatrix[10] == 0) throw new RuntimeException("1: to world cords failed, div by zero");
        float gl_x =((point.x) * 2.0f / width - 1.0f);
        float gl_y = ((height-point.y) * 2.0f / height - 1.0f);
        float gl_z = (invTransformMatrix[2]*gl_x + invTransformMatrix[6]*gl_y + invTransformMatrix[14] -z_out) / -invTransformMatrix[10];

        //System.out.println("gl_z" + gl_z);
        float[] pointInGL = new float[]{gl_x,gl_y,gl_z,1};
        float[] ret = new float[4];
        Matrix.multiplyMV(ret, 0, invTransformMatrix, 0, pointInGL, 0);
        //System.out.println("ret z" + ret[2]);

        // avoid div with 0. Don't know if this is a problem
        if (ret[3] == 0.0)
            throw new RuntimeException("2: to world cords failed, div by zero");

        //div so w is one again.
        return new Vector2(ret[0] / ret[3], ret[1] / ret[3]);
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
