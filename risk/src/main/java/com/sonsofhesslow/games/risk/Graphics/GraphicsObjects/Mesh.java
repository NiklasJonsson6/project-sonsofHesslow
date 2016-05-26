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
package com.sonsofhesslow.games.risk.graphics.graphicsObjects;

import com.sonsofhesslow.games.risk.graphics.utils.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;

/**
 * A Mesh based on developer.android.com's Square.
 */
public class Mesh {

    FloatBuffer vertexBuffer;
    ShortBuffer drawListBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    final short[] triangles;
    final Vector2[] vertices;
    final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex (coord right? //daniel)

    public boolean isOnMesh2D(Vector2 point)
    {
        int currentTri = 0;
        if(point.x<minX || point.x>maxX || point.y < minY || point.y > maxY)return false;
        while(triangles.length/3> currentTri)
        {
            Vector2 p0  = vertices[triangles[currentTri * 3 + 0]];
            Vector2 p1  = vertices[triangles[currentTri * 3 + 1]];
            Vector2 p2 = vertices[triangles[currentTri * 3 + 2]];
            ++currentTri;
            if(Vector2.isInsideTri(point, p0, p1, p2))
            {
                return true;
            }
        }
        return false;
    }

    private final float[] new_verts;
    public Mesh(short[] triangles, Vector2[] vertices)
    {
        this.vertices = vertices;
        this.triangles = triangles;
        new_verts = new float[vertices.length*COORDS_PER_VERTEX];
        for(int i = 0; i<vertices.length;i++)
        {
            new_verts[i*COORDS_PER_VERTEX]   = vertices[i].x;
            new_verts[i*COORDS_PER_VERTEX+1] = vertices[i].y;
            new_verts[i*COORDS_PER_VERTEX+2] = 0;
        }
        calculateMetrics();
    }
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    private void calculateMetrics()
    {
        Vector2[] verts = vertices;
        minX = verts[0].x;
        maxX = verts[0].x;
        minY = verts[0].y;
        maxY = verts[0].y;
        for(int i = 1; i< verts.length;i++)
        {
            minX = Math.min(minX,verts[i].x);
            minY = Math.min(minY,verts[i].y);
            maxX = Math.max(maxX,verts[i].x);
            maxY = Math.max(maxY,verts[i].y);
        }
    }


    public void init() {

        if(triangles.length%3 != 0){
            throw new IllegalArgumentException("A triangle array needs to be divisible by three");
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(new_verts.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(new_verts);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(triangles.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(triangles);
        drawListBuffer.position(0);
    }

    public static Mesh Add(Mesh a, Mesh b)
    {
        Vector2[] new_verts = ArrayUtils.concat(a.vertices,b.vertices);
        int at_len = a.triangles.length;
        int bt_len = b.triangles.length;

        short[] new_tris = new short[at_len + bt_len];
        System.arraycopy(a.triangles, 0, new_tris, 0, at_len);
        for(int i = 0;i<bt_len;i++) {
            new_tris[at_len+i] = (short)(b.triangles[i]+a.vertices.length);
        }
        return new Mesh(new_tris, new_verts);
    }
}
