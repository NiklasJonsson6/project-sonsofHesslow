package com.sonsofhesslow.games.risk.graphics.GraphicsObjects;

import android.opengl.Matrix;

import com.sonsofhesslow.games.risk.graphics.Geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.Geometry.Vector3;
import com.sonsofhesslow.games.risk.graphics.MyGLRenderer;

/**
 * Created by daniel on 3/31/16.
 */
public class Camera {
    public Camera() {
        updateLookAt();
    }

    private static Camera instance;

    public static Camera getInstance() {
        if (instance == null) instance = new Camera();
        return instance;
    }

    public float stitchPosition;
    public Vector3 pos = new Vector3(-5, -5, -3); //-5-5 is about the current center..
    public Vector3 lookAt;
    public Vector3 up = new Vector3(0, 1, 0);

    float WORLD_MIN_X = -16;
    float WORLD_MAX_X = 0;

    float WORLD_MIN_Y = -12;
    float WORLD_MAX_Y = 5;

    float clamp(float f, float min, float max) {
        return Math.min(Math.max(min, f), max);
    }

    public void setPos(Vector3 newPos) {

            float width = MyGLRenderer.viewPortToWorldCoord(new Vector2(-1, 0), 0).x
                    - MyGLRenderer.viewPortToWorldCoord(new Vector2(+1, 0), 0).x;

            float height = MyGLRenderer.viewPortToWorldCoord(new Vector2(0, -1), 0).y
                    - MyGLRenderer.viewPortToWorldCoord(new Vector2(0, 1), 0).y;


            width = Math.abs(width);
            height = Math.abs(height);

            float min_x = WORLD_MIN_X + width / 2;
            float max_x = WORLD_MAX_X - width / 2;
            float min_y = WORLD_MIN_Y + height / 2;
            float max_y = WORLD_MAX_Y - height / 2;

            float WORLD_WIDTH = WORLD_MAX_X - WORLD_MIN_X;
            float new_x = newPos.x;
            float new_y = clamp(newPos.y, min_y, max_y);

            if (newPos.x < min_x) {
                stitchPosition = (1 - (min_x - newPos.x) / width);
            } else if (newPos.x > max_x) {
                stitchPosition = (-((max_x - newPos.x) / width));
            } else {
                stitchPosition = 0;
            }

            if (stitchPosition > 1f) {
                new_x -= WORLD_WIDTH;
            }
            if (stitchPosition < 0.0f) {
                new_x += WORLD_WIDTH;
            }

            pos = new Vector3(new_x, new_y, newPos.z);
            updateLookAt();
    }

    public Camera[] getStitchCams() {
        float width = MyGLRenderer.viewPortToWorldCoord(new Vector2(-1, 0), 0).x
                - MyGLRenderer.viewPortToWorldCoord(new Vector2(+1, 0), 0).x;
        width = Math.abs(width);

        Camera left = new Camera();
        Camera right = new Camera();
        left.pos = new Vector3(WORLD_MIN_X + width * stitchPosition / 2, pos.y, pos.z);
        right.pos = new Vector3(WORLD_MAX_X - width * (1 - stitchPosition) / 2, pos.y, pos.z);

        left.updateLookAt();
        right.updateLookAt();
        return new Camera[]{left, right};
    }

    public void setPosRel(Vector3 newPos) {
        setPos(Vector3.Add(newPos, pos));
        updateLookAt();
    }

    private void updateLookAt() {
        lookAt = new Vector3(pos.x, pos.y, 0);
    }

    public float[] getViewMatrix() {
        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, pos.x, pos.y, pos.z, lookAt.x, lookAt.y, lookAt.z, up.x, up.y, up.z);
        return viewMatrix;
    }
}
