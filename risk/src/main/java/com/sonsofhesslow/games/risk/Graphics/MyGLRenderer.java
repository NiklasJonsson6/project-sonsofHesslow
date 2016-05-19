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

    public void delayedInit(GLObject m) {
        objectsToBeAdded.add(m);
    }

    public void remove(GLObject object) {
        objectsToBeRemoved.add(object);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        frameInit();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Camera cam = Camera.getInstance();
        Matrix.setLookAtM(viewMatrix, 0, cam.pos.x, cam.pos.y, cam.pos.z, cam.lookAt.x, cam.lookAt.y, cam.lookAt.z, cam.up.x, cam.up.y, cam.up.z);

        if (cam.stitchPosition > 0 && cam.stitchPosition < 1) {
            Camera[] cams = cam.getStitchCams();
            int x = (int) (width * cam.stitchPosition);
            if (x != 0 && x != width) {
                render(0, 0, x, height, cams[0]);
                render(x, 0, width, height, cams[1]);
                return;
            }
        }
        render(0, 0, width, height, cam);
    }

    private void frameInit() {
        for (GLObject go : objectsToBeRemoved) {
            objectsToBeAdded.remove(go);
            gameObjects.remove(go);
        }
        objectsToBeRemoved.clear();
        for (GLObject go : objectsToBeAdded) {
            go.gl_init();
            gameObjects.add(go);
        }
        objectsToBeAdded.clear();
    }

    private void render(int left, int bottom, int right, int top, Camera camera) {
        int width = right - left;
        int height = top - bottom;
        GLES20.glViewport(left, bottom, width, height);

        float[] viewMatrix = camera.getViewMatrix();
        float[] projectionMatrix = calculateProjectionMatrix(height, width);
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Collections.sort(gameObjects, new Comparator<GLObject>() {
            @Override
            public int compare(GLObject lhs, GLObject rhs) {
                return Float.compare(lhs.drawOrder, rhs.drawOrder);
            }
        });
        for (GLObject go : gameObjects) {
            if (go.isActive)
                go.draw(MVPMatrix);
        }
    }

    private static float[] calculateProjectionMatrix(float height, float width) {
        float[] projectionMatrix = new float[16];
        float ratio = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 20);
        return projectionMatrix;
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

    public static Vector2 viewPortToWorldCoord(Vector2 point, float z_out, float[] projectionMatrix, float[] viewMatrix) {
        float[] transformMatrix = new float[16];
        Matrix.multiplyMM(transformMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        float[] invTransformMatrix = new float[16];
        Matrix.invertM(invTransformMatrix, 0, transformMatrix, 0);
        if (invTransformMatrix[10] == 0) {
            throw new RuntimeException("my bad // gl form viewport coords");
        }
        float gl_x = point.x;
        float gl_y = point.y;
        float gl_z = (invTransformMatrix[2] * gl_x + invTransformMatrix[6] * gl_y + invTransformMatrix[14] - z_out) / -invTransformMatrix[10];

        float[] pointInGL = new float[]{gl_x, gl_y, gl_z, 1};
        float[] ret = new float[4];
        Matrix.multiplyMV(ret, 0, invTransformMatrix, 0, pointInGL, 0);

        // avoid div with 0. Don't know if this is a problem
        if (ret[3] == 0.0)
            throw new RuntimeException("2: viewPort to world cords failed, div by zero");

        return new Vector2(ret[0] / ret[3], ret[1] / ret[3]);
    }

    public static Vector2 viewPortToWorldCoord(Vector2 point, float z_out) {
        return viewPortToWorldCoord(point, z_out, projectionMatrix, viewMatrix);
    }

    public static Vector2 screenToWorldCoords(Vector2 point, float z_out, int width, int height, float[] viewMatrix) {
        float gl_x = ((point.x) * 2.0f / width - 1.0f);
        float gl_y = ((height - point.y) * 2.0f / height - 1.0f);
        return viewPortToWorldCoord(new Vector2(gl_x, gl_y), z_out, calculateProjectionMatrix(height, width), viewMatrix);
    }

    public static Vector2 screenToWorldCoords(Vector2 point, float z_out) {
        return screenToWorldCoords(point, z_out, width, height, viewMatrix);
    }

    public static Vector2 screenToWorldCoors_stitched(Vector2 point, float z_out) {
        Camera cam = Camera.getInstance();
        if (cam.stitchPosition > 0 && cam.stitchPosition < 1) {
            Camera[] cams = cam.getStitchCams();
            int x = (int) (width * cam.stitchPosition);
            if (x != 0 && x != width) {
                if (point.x < x) {//left
                    return screenToWorldCoords(
                            new Vector2(point.x, point.y),
                            z_out,
                            x,
                            height,
                            cams[0].getViewMatrix());
                } else {//right
                    return screenToWorldCoords(
                            new Vector2(point.x - x, point.y),
                            z_out,
                            width - x,
                            height,
                            cams[1].getViewMatrix());
                }
            }
        }
        return screenToWorldCoords(new Vector2(point.x, point.y), z_out);
    }
}
