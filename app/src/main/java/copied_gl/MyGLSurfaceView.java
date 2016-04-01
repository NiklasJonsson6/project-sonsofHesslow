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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import gl_own.Camera;
import gl_own.Geometry.Vector2;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if(MyGLRenderer.beizier.m.isOnMesh2D(new Vector2(x,y)))
                {
                    float[] color = {0.7f,0.9f,0.2f,1f};
                    MyGLRenderer.beizier.m.color = color;
                }
                else
                {
                    float[] color = {0.2f,0.9f,0.7f,1f};
                    MyGLRenderer.beizier.m.color = color;
                }
                System.out.println("pointer down");
                break;
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                dx/=1000;
                dy/=1000;
                Camera cam = Camera.getInstance();
                float[] newPos={cam.X()+dx,cam.Y()+dy,cam.Z()};

                //cam.setPos(newPos);
                Vector2 gl_cord = MyGLRenderer.ScreentoGLCoords(new Vector2(x,y));

                System.out.println("x:"+gl_cord.x + ",y:" + gl_cord.y);

                if(MyGLRenderer.beizier.m.isOnMesh2D(new Vector2(x,y)))
                {
                    float[] color = {0.7f,0.9f,0.2f,1f};
                    MyGLRenderer.beizier.m.color = color;
                }
                else
                {
                    float[] color = {0.2f,0.9f,0.7f,1f};
                    MyGLRenderer.beizier.m.color = color;
                }
                requestRender();
                break;
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

}
