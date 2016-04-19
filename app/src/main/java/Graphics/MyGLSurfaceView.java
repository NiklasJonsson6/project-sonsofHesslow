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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import Graphics.GraphicsObjects.FilledBeizierPath;
import Graphics.Geometry.Vector2;

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

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
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


    /*public MyGLSurfaceView(Context context) {
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
    }*/

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private Vector2 prevPos;


    private ConcurrentLinkedQueue<GL_TouchListener> listeners = new ConcurrentLinkedQueue<>();
    public void addListener(GL_TouchListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        Vector2 screen_pos = new Vector2(e.getX(),e.getY());
        Vector2 world_pos = MyGLRenderer.ScreenToWorldCoords(screen_pos,0);

        int index = 0;
        boolean hasTouchedRegion = false;
        for(FilledBeizierPath path : GraphicsManager.beiziers)
        {
            if(path.fill_mesh.isOnMesh2D(world_pos))
            {
                hasTouchedRegion = true;
                break;
            }
            ++index;
        }
        if(!hasTouchedRegion) index = -1;

        GL_TouchEvent event = new GL_TouchEvent(e, hasTouchedRegion, index, world_pos, screen_pos);
        for(GL_TouchListener listener:listeners)
        {
            listener.Handle(event);
        }

        return true;
    }


}

