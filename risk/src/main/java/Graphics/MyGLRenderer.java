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

import Graphics.GraphicsObjects.GLObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import Graphics.GraphicsObjects.Camera;
import Graphics.Geometry.Vector2;
import Graphics.GraphicsObjects.Renderer;
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

public class MyGLRenderer implements GLSurfaceView.Renderer, Renderer {

    public static final float[] mMVPMatrix = new float[16];

    private static final float[] mProjectionMatrix = new float[16];
    private static final float[] mViewMatrix = new float[16];
    private float mAngle;
    private static List<GLObject> gameObjects = new ArrayList<>();
    private static MyGLRenderer ref = null;

    MyGLRenderer getInstance()
    {
        if(ref == null) ref = new MyGLRenderer();
        return ref;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(1f, 1f, 1f, 1.0f);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    static ConcurrentLinkedQueue<GLObject> objectsToBeAdded = new ConcurrentLinkedQueue<>();
    static ConcurrentLinkedQueue<GLObject> objectsToBeRemoved = new ConcurrentLinkedQueue<>();

    public void delayedInit(GLObject m)
    {
        objectsToBeAdded.add(m);
    }
    public void remove(GLObject object)
    {
        objectsToBeRemoved.add(object);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        for(GLObject go : objectsToBeRemoved)
        {
            objectsToBeAdded.remove(go);
            gameObjects.remove(go);
        }
        objectsToBeRemoved.clear();
        for(GLObject go : objectsToBeAdded)
        {
            go.gl_init();
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
        Collections.sort(gameObjects, new Comparator<GLObject>() {
            @Override
            public int compare(GLObject lhs, GLObject rhs) {
                return Float.compare(lhs.drawOrder, rhs.drawOrder);
            }
        });
        for(GLObject go : gameObjects)
        {
            if(go.isActive)
                go.draw(mMVPMatrix);
        }
    }

    private static int width;
    private static int height;

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

    public static Vector2 ScreenToWorldCoords(Vector2 point,float z_out)
    {
        float[] transformMatrix = new float[16];
        Matrix.multiplyMM(transformMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        float[] invTransformMatrix = new float[16];
        Matrix.invertM(invTransformMatrix, 0, transformMatrix, 0);

        if(invTransformMatrix[10] == 0)
        {
            //this only happens if opengl hasn't finished to initialize stuff yet.
            return point; // this is probably the best we can do.
            //throw new RuntimeException("1: to world cords failed, div by zero");
        }
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
