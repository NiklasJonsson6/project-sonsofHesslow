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
package gl_own;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.sql.SQLOutput;
import java.util.Iterator;

import copied_gl.MyGLRenderer;
import gl_own.Geometry.Util;
import gl_own.Geometry.Vector2;

/**
 * A Mesh based on developer.android.com's Square.
 */
public class Mesh {

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    short[] triangles;
    Vector2[] vertices;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex (coord right? //daniel)

    public float color[];

    class Triangle
    {
        public Triangle(Vector2 a, Vector2 b, Vector2 c)
        {
            points = new Vector2[]{a,b,c};
        }
        Vector2[] points;

        @Override
        public String toString()
        {
            return "("+points[0].toString()+", "+points[1].toString()+", "+points[2].toString()+")";
        }
    }

    class MeshIterator implements Iterator
    {
        Mesh mesh;
        int currentTriangle;
        public MeshIterator(Mesh mesh)
        {
            this.mesh = mesh;
            this.currentTriangle = 0;
        }
        @Override
        public boolean hasNext() {
            return mesh.triangles.length/3> currentTriangle;
        }


        @Override
        public Triangle next() {
            Vector2 a = mesh.vertices[mesh.triangles[currentTriangle * 3 + 0]];
            Vector2 b = mesh.vertices[mesh.triangles[currentTriangle*3+1]];
            Vector2 c = mesh.vertices[mesh.triangles[currentTriangle*3+2]];

            ++currentTriangle;
            return new Triangle(a,b,c);
        }

        @Override
        public void remove() {
            throw new RuntimeException("Meshes don't support removal of triangles at this moment");
        }
    }

    public MeshIterator meshIterator()
    {
        return new MeshIterator(this);
    }

    public boolean isOnMesh2D(Vector2 point)
    {

        MeshIterator it = meshIterator();
        while(it.hasNext())
        {
            Triangle triangle = it.next();
            Vector2 p0 = triangle.points[0];
            Vector2 p1 = triangle.points[1];
            Vector2 p2 = triangle.points[2];
            if(Util.isInsideTri(point, p0, p1, p2))
            {
                /*
                    float[] color_outside = new float[]{0.6f,0.2f,0.8f,1};
                    float[] color_inside = new float[]{0.3f,0.9f,0.6f,1};
                    MyGLRenderer.addSquare(point,color_inside);
                    MyGLRenderer.addTri(p0, p1, p2, color_outside);
                 */
                return true;
            }
        }
        System.out.println("none inside");

        return false;
    }

    public Mesh(short[] triangles, Vector2[] vertices, float color[])
    {
        this.vertices = vertices;
        this.triangles = triangles;
        this.color = color;

        float[] new_verts = new float[vertices.length*COORDS_PER_VERTEX];
        for(int i = 0; i<vertices.length;i++)
        {
            new_verts[i*COORDS_PER_VERTEX]   = vertices[i].x;
            new_verts[i*COORDS_PER_VERTEX+1] = vertices[i].y;
            new_verts[i*COORDS_PER_VERTEX+2] = 0;
        }
        init(triangles, new_verts, color);
    }



    // because java is stupid and constructors need to be called on the first line on other constructors.
    // here is the meat of the constructors.
    private void init(short[] triangles, float[] vertices, float color[]) {

        if(color.length != 4){
            throw new IllegalArgumentException("a color consists of 4 values, r g b a. not " + color.length);
        }
        if(triangles.length%3 != 0){
            throw new IllegalArgumentException("A triangle array needs to be divisible by three");
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                triangles.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(triangles);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader    = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader  = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }


    public void draw(float[] matrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);


        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, triangles.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
