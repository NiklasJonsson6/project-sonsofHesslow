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

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import gl_own.Camera;
import gl_own.FilledBeizierPath;
import gl_own.Geometry.Vector2;
import gl_own.Geometry.Vector3;
import gl_own.Square;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    class MyConfigChooser implements GLSurfaceView.EGLConfigChooser {
        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int attribs[] = {
                    EGL10.EGL_LEVEL, 0,
                    EGL10.EGL_RENDERABLE_TYPE, 4,  // EGL_OPENGL_ES2_BIT
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_TRANSPARENT_RGB,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE,8,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_SAMPLE_BUFFERS, 1,
                    EGL10.EGL_SAMPLES, 4,
                    EGL10.EGL_STENCIL_SIZE,2,
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] configCounts = new int[1];
            egl.eglChooseConfig(display, attribs, configs, 1, configCounts);

            if (configCounts[0] == 0) {
                // Failed! Error handling.
                return null;
            } else {
                return configs[0];
            }
        }
    }

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        super.setEGLConfigChooser(new MyConfigChooser());
        super.setEGLConfigChooser(8,8,8,8,16,0);
        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private Vector2 prevPos;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();
        Vector2 pos = new Vector2(x,y);
        Vector3 posWP = MyGLRenderer.ScreenToGl(pos);
        Vector3 prevposWP=null;
        if(prevPos!=null)
            prevposWP = MyGLRenderer.ScreenToGl(prevPos);

        Vector2 checkPos = MyGLRenderer.ScreenToWorldCoords(pos,0.55555555555f).ToVector2();

        if(prevposWP!=null && posWP!=null)
        {
            System.out.println("fucking pos = "+ checkPos);
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    Vector2 delta = Vector2.Sub(prevposWP.ToVector2(), posWP.ToVector2());
                    Camera cam = Camera.getInstance();
                    float[] newPos={cam.X()-delta.x,cam.Y()-delta.y,-3};
                    cam.setPos(newPos);

                    for(FilledBeizierPath path : MyGLRenderer.beiziers)
                    {
                        if(path.mesh.isOnMesh2D(checkPos))
                        {
                            float[] color = {(float)Math.random(),(float)Math.random(),(float)Math.random(),1f};
                            path.mesh.color = color;
                        }
                    }

                    requestRender();
                    break;
            }
        }

        prevPos = pos;
        return true;
    }


}
