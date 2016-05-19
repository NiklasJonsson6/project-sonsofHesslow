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
package com.sonsofhesslow.games.risk.graphics;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.sonsofhesslow.games.risk.graphics.GraphicsObjects.GLObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.sonsofhesslow.games.risk.graphics.GraphicsObjects.Camera;
import com.sonsofhesslow.games.risk.graphics.Geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.GraphicsObjects.Renderer;
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

    public static final float[] MVPMatrix = new float[16];
    private static final float[] projectionMatrix = new float[16];
    private static final float[] viewMatrix = new float[16];
    private static List<GLObject> gameObjects = new ArrayList<>();

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
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
        frame_init();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Camera cam = Camera.getInstance();
        Matrix.setLookAtM(viewMatrix, 0, cam.pos.x, cam.pos.y, cam.pos.z, cam.lookAt.x, cam.lookAt.y, cam.lookAt.z, cam.up.x, cam.up.y, cam.up.z);

        if(cam.stitchPostion > 0 && cam.stitchPostion < 1)
        {
            Camera[] cams = cam.getStitchCams();
            int x =(int)(width*cam.stitchPostion);
            if(x!=0 && x != width)
            {
                render(0, 0, x, height, cams[0]);
                render(x, 0, width, height,cams[1]);
                return;
            }
        }
        render(0, 0, width, height, cam);
    }
    private void frame_init()
    {
        for(GLObject go : objectsToBeRemoved) {
            objectsToBeAdded.remove(go);
            gameObjects.remove(go);
        }
        objectsToBeRemoved.clear();
        for(GLObject go : objectsToBeAdded) {
            go.gl_init();
            gameObjects.add(go);
        }
        objectsToBeAdded.clear();
    }
    private void render(int left, int bottom, int right, int top, Camera camera)
    {
        int width = right-left;
        int height = top-bottom;

        GLES20.glViewport(left, bottom, width, height);
        float[] projectionMatrix = new float[16];
        float ratio = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 20);

        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, camera.pos.x, camera.pos.y, camera.pos.z, camera.lookAt.x, camera.lookAt.y, camera.lookAt.z, camera.up.x, camera.up.y, camera.up.z);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Collections.sort(gameObjects, new Comparator<GLObject>() {
            @Override
            public int compare(GLObject lhs, GLObject rhs) {
                return Float.compare(lhs.drawOrder, rhs.drawOrder);
            }
        });
        for(GLObject go : gameObjects) {
            if(go.isActive)
                go.draw(MVPMatrix);
        }
    }


    private static int width;
    private static int height;

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        this.width = width;
        this.height = height;

        float ratio = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 20);
    }

    public static Vector2 ViewPortToWorldCoord(Vector2 point, float z_out)
    {
        float[] transformMatrix = new float[16];
        Matrix.multiplyMM(transformMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        float[] invTransformMatrix = new float[16];
        Matrix.invertM(invTransformMatrix, 0, transformMatrix, 0);

        if(invTransformMatrix[10] == 0)
        {
            throw new RuntimeException("my bad // gl form viewport coords");
        }
        float gl_x = point.x;
        float gl_y = point.y;
        float gl_z = (invTransformMatrix[2]*gl_x + invTransformMatrix[6]*gl_y + invTransformMatrix[14] -z_out) / -invTransformMatrix[10];

        float[] pointInGL = new float[]{gl_x,gl_y,gl_z,1};
        float[] ret = new float[4];
        Matrix.multiplyMV(ret, 0, invTransformMatrix, 0, pointInGL, 0);

        // avoid div with 0. Don't know if this is a problem
        if (ret[3] == 0.0)
            throw new RuntimeException("2: viewPort to world cords failed, div by zero");

        return new Vector2(ret[0] / ret[3], ret[1] / ret[3]);
    }
    public static Vector2 ScreenToWorldCoords(Vector2 point, float z_out)
    {
        float[] transformMatrix = new float[16];
        Matrix.multiplyMM(transformMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

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
        if (Math.abs(ret[3]) < 0.0001)
            throw new RuntimeException("2: to world cords failed, div by zero");

        //div so w is one again.
        return new Vector2(ret[0] / ret[3], ret[1] / ret[3]);
    }
}
