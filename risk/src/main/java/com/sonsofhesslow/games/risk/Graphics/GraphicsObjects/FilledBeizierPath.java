package com.sonsofhesslow.games.risk.graphics.GraphicsObjects;

import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import com.sonsofhesslow.games.risk.graphics.Geometry.BeizierPath;
import com.sonsofhesslow.games.risk.graphics.Geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.Geometry.Vector3;

/**
 * Created by daniel on 3/31/16.
 */
public class FilledBeizierPath extends GLObject implements Updatable {


    public Mesh fill_mesh;
    private Mesh outline_mesh;
    private FlowShader flowShader;
    private LineShader lineShader;
    @Override
    public void gl_init() {
        lineShader = new LineShader();
        flowShader = new FlowShader();
        fill_mesh.init();
        outline_mesh.init();
    }

    private Vector2 center;
    private float Area;
    private void calcCenter()
    {
        // from https://en.wikipedia.org/wiki/Centroid
        float x=0;
        float y=0;
        float A=0;
        for(int i = 0;i<fill_mesh.vertices.length;i++)
        {
            Vector2 curr = fill_mesh.vertices[i];
            Vector2 next = fill_mesh.vertices[(i+1)%fill_mesh.vertices.length];
            float a = curr.x*next.y - next.x*curr.y;
            x+= (curr.x+next.x) * a;
            y+= (curr.y+next.y) * a;
            A += a;
        }
        Area = Math.abs(A/2f);
        float nrm = 1f/(3f*A);
        center = new Vector2(x * nrm,y * nrm);
    }


    private final int naive_precision = 2; //higher is more detailed

    public BeizierPath path;
    public Vector2 getCenter()
    {
        return  center;
    }
    private FloatBuffer vertSide;
    boolean doRest=false;
    public FilledBeizierPath(BeizierPath path, Renderer renderer) // start ctl ctl point ctl ctl point ctl ctl (start)
    {
        super(renderer);
        if(!path.isClosed()) throw new IllegalArgumentException("the beizier path needs to be closed!");
        this.path = path;

        Vector2[] verts = path.approximateBeizierPath_naive(naive_precision);
        Vector2[] outline_verts = new Vector2[verts.length*2];

        //while the triangles are not guaranteed to be non-overlapping constant winding.
        // the way we render the triangles we just dont care.
        short[] outline_tris = new short[6*outline_verts.length];
        float[] vertSide_arr = new float[outline_verts.length];
        for(int i = 0;i<verts.length;i++)
        {
            Vector2 prev = verts[i];
            Vector2 current = verts[(i+1)%verts.length];
            Vector2 next = verts[(i+2)%verts.length];
            Vector2 diffa = Vector2.Sub(next,current).normalized();
            Vector2 diffb = Vector2.Sub(current,prev).normalized();
            Vector2 diff = Vector2.Add(diffa,diffb).normalized();
            float scaleFactor = Math.max(Math.abs(Vector2.dot(diff,diffa)),0.8f);
            Vector2 orth = Vector2.Mul(new Vector2(-diff.y,diff.x),1/scaleFactor*0.01f);

            outline_verts[i*2+0] = Vector2.Add(current, orth);
            outline_verts[i*2+1] = Vector2.Sub(current, orth);

            vertSide_arr[i*2+0] = 0;
            vertSide_arr[i*2+0] = 1;

            outline_tris[i*6+0] =(short)(i*2+0);
            outline_tris[i*6+1] =(short)(i*2+1);
            outline_tris[i*6+2] =(short)((i*2+2)%outline_verts.length);

            outline_tris[i*6+3] =(short)(i*2+1);
            outline_tris[i*6+4] =(short)((i*2+2)%outline_verts.length);
            outline_tris[i*6+5] =(short)((i*2+3)%outline_verts.length);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(vertSide_arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertSide = bb.asFloatBuffer();
        vertSide.put(vertSide_arr);
        vertSide.position(0);

        outline_mesh = new Mesh(outline_tris,outline_verts);


        //finding out the most prominent winding order
        float wind_ack = 0;
        for(int i = 0; i<verts.length;i++)
        {
            Vector2 cur = verts[i];
            Vector2 next = verts[(i+1) % verts.length];
            wind_ack +=(next.x-cur.x)*(next.y+cur.y);
        }
        float winding = Math.signum(wind_ack);

        //setup the remaining vertex indices
        short[] tris = new short[(verts.length-2)*3];
        int current_index = 0;
        List<Integer> remainingIndices = new LinkedList<>();
        for(int i = 0; i<verts.length;i++)
        {
            remainingIndices.add(i);
        }
        //triangulation by earclipping not perfect....
        while(remainingIndices.size() >= 3)
        {
            System.out.println("earclipping " + remainingIndices.size() + " left");
            boolean removed = false;
            float acceptable_concavity = 0;
            for(int i = 0;i<remainingIndices.size();i++)
            {
                int index_a = remainingIndices.get(i);
                int index_b = remainingIndices.get((i+1) % remainingIndices.size());
                int index_c = remainingIndices.get((i+2) % remainingIndices.size());

                Vector2 a = verts[index_a];
                Vector2 b = verts[index_b];
                Vector2 c = verts[index_c];

                //only add the tri if it's inside the polygon
                float concavity = Vector2.crossProduct(a, b, c);
                if (Math.signum(concavity)!=winding||Math.abs(concavity)<=acceptable_concavity||doRest)
                {
                    //check if there is any other vertex inside our proposed triangle
                    boolean noneInside = true;
                    if(!doRest)
                        for(int j = 0; j<verts.length;j++)
                        {
                            if(j == index_a || j == index_b || j == index_c)continue;
                            if (Vector2.isInsideTri(verts[j], a, b, c))
                            {
                                noneInside = false;
                                break;
                            }
                        }

                    if(noneInside||doRest)
                    {
                        //add the triangle and remove the middle vertex from further consideration
                        tris[current_index++] = (short) index_a;
                        tris[current_index++] = (short) index_b;
                        tris[current_index++] = (short) index_c;

                        remainingIndices.remove((i+1) % remainingIndices.size());
                        removed = true;
                        if(remainingIndices.size() == 2) break;
                    }
                }
            }
            if (!removed)
            {
                System.out.println("not all tris was drawn.. is it self intersecting or is the precision set very high?");
                doRest = true;
            }
            if(remainingIndices.size() <= 2) break;
        }

        fill_mesh = new Mesh(tris, verts);
        calcCenter();
        origin = new Vector3(center,0);
    }
    public void setColorOutline(float[] color)
    {
        outlineColor = color;
    }

    public void mergeWith(FilledBeizierPath other)
    {
        fill_mesh = Mesh.Add(fill_mesh,other.fill_mesh);
        outline_mesh= Mesh.Add(outline_mesh,other.outline_mesh);

        ByteBuffer bb = ByteBuffer.allocateDirect((vertSide.capacity() + other.vertSide.capacity()) * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertSide_new = bb.asFloatBuffer();
        vertSide_new.put(vertSide);
        vertSide_new.put(other.vertSide);
        vertSide_new.position(0);
        vertSide = vertSide_new;
        center = Vector2.Mul(Vector2.Add(Vector2.Mul(center, Area), Vector2.Mul(other.center, other.Area)),1f/(other.Area+Area));
    }

    private float[] fromColor= {0.7f,0.7f,0.7f,1f};
    private float[] toColor= {0.7f,0.7f,0.7f,1f};
    private float[] outlineColor = new float[]{0,0,0,1} ;
    private float max_len=20;
    private float len=20;
    private Vector3 origin;
    public void setColor(float[] color, Vector2 origin)
    {
        this.origin = new Vector3(origin,0);
        max_len = 20;
        len = 0;
        fromColor = toColor;
        toColor = color;
    }

    public void setColor(float[] color)
    {
        setColor(color,center);
    }





    @Override
    public boolean update(float dt) {
        if(Math.abs(max_len-len)>0.01) {
            len += (max_len-len)/200;
            return true;
        }
        return false;
    }


    public void draw(float[] projectionMatrix){
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
        flowShader.use(fill_mesh, mvpMatrix, origin, len,toColor,fromColor);
        lineShader.use(outline_mesh, mvpMatrix, outlineColor,vertSide);
    }
}
