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
import java.util.Arrays;
import java.util.Iterator;

import copied_gl.MyGLRenderer;
import copied_gl.MyGLSurfaceView;
import gl_own.Geometry.Util;
import gl_own.Geometry.Vector2;

/**
 * A Mesh based on developer.android.com's Square.
 */
public class Mesh {
    public float[] matrix;

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // The matrix must be included as a modifier of gl_Position.
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
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
    float[] vertices;

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
            return mesh.triangles.length-2> currentTriangle*3;
        }

        public Vector2 getVertexAt(int index)
        {
            int vertIndexStart = mesh.triangles[index];
            return new Vector2(mesh.vertices[vertIndexStart],
                      mesh.vertices[vertIndexStart+1]);
        }

        @Override
        public Triangle next() {

            Vector2 a = getVertexAt(currentTriangle*3+0);
            Vector2 b = getVertexAt(currentTriangle*3+1);
            Vector2 c = getVertexAt(currentTriangle*3+2);

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

    //dude fix me yo..
    //use ray-tracing maybe??
    public boolean isOnMesh2D(Vector2 point)
    {
        point = MyGLRenderer.ScreentoGLCoords(point);

        float[] tmp = new float[]{point.x,point.y,0,1};
        float[] scratch = new float[16];

        Matrix.invertM(scratch,0,matrix,0);

        Matrix.multiplyMV(tmp,0,scratch,0,tmp,0);
        point.x = tmp[0];
        point.y = tmp[1];

        System.out.println(point);

        MeshIterator it = meshIterator();
        int counter = 0;
        while(it.hasNext())
        {
            Triangle triangle = it.next();
            Vector2 p0 = triangle.points[0];
            Vector2 p1 = triangle.points[1];
            Vector2 p2 = triangle.points[2];

            float s = p0.y * p2.x - p0.x * p2.y + (p2.y - p0.y) * point.x + (p0.x - p2.x) * point.y;
            float t = p0.x * p1.y - p0.y * p1.x + (p0.x - p1.y) * point.x + (p1.x - p0.x) * point.y;

            if ((s < 0) != (t < 0))
                continue;

            float A = -p1.y * p2.x + p0.y * (p2.x - p1.x) + p0.x * (p1.y - p2.y) + p1.x * p2.y;
            if (A < 0.0)
            {
                s = -s;
                t = -t;
                A = -A;
            }
            if((s > 0 && t > 0 && (s + t) <= A))
            {
               return true;
            }

        }

        return false;
    }

    public Mesh(short[] triangles, Vector2[] vertices, float color[],float[] matrix)
    {
        float[] new_verts = new float[vertices.length*3];
        for(int i = 0; i<vertices.length;i++)
        {
            new_verts[i*3]   = vertices[i].x;
            new_verts[i*3+1] = vertices[i].y;
            new_verts[i*3+2] = 0;
        }
        init(triangles, new_verts, color,matrix);
    }

    public Mesh(short[] triangles, float[] vertices, float color[], float[] matrix)
    {
        init(triangles,vertices,color,matrix);
    }

    // because java is stupid and constructors need to be called on the first line on other constructors.
    // here is the meat of the constructors.
    private void init(short[] triangles, float[] vertices, float color[],float[] matrix) {

        if(color.length != 4){
            throw new IllegalArgumentException("a color consists of 4 values, r g b a. not " + color.length);
        }
        if(triangles.length%3 != 0){
            throw new IllegalArgumentException("A triangle array needs to be divisible by three");
        }
        if(matrix.length != 16)
        {
            throw new IllegalArgumentException("A 4x4 matrix has 16 entries");
        }

        //no uvs yet. do we care about texturing??
        this.triangles = triangles;
        this.vertices = vertices;
        this.color = color;
        this.matrix = matrix;

        // initialize vertex byte buffer for shape coordinates
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
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }


    public void draw() {
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
