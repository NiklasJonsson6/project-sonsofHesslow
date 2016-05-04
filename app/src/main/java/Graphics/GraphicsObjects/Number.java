package Graphics.GraphicsObjects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.text.TextPaint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import Graphics.Geometry.Vector2;
import Graphics.Geometry.Vector3;
import Graphics.MyGLRenderer;

/**
 * Created by Daniel on 04/05/2016.
 */


// renders numbers onto textures and then displays them as textured quads.
// textures are cashed and reused.
public class Number extends GLObject{
    static int[] textures = null;

    short[] tris;
    Vector2[] verts;
    private int num=-1;

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "varying vec2 u_tc;"+
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  u_tc = -vec2(vPosition);"+
                    "}";

    private final String fragmentShaderCode =

            "precision mediump float;" +
                    "uniform sampler2D u_Texture;"+
                    "uniform vec4 u_Color;"+
                    "varying vec2 u_tc;"+
                    "void main() {" +
                    "  gl_FragColor = u_Color * texture2D(u_Texture, u_tc);"+
                    "}";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex (coord right? //daniel)

    float[] new_verts;
    public void setValue(int value) {
        num = value;
    }
    public Number(int value) {
        if(textures == null)
        {
            textures = new int[200];
            for(int i = 0; i<textures.length;i++)
            {
                textures[i]=-1;
            }
        }
        {
            Vector2 top_rigth =     new Vector2(1,1);
            Vector2 top_left =      new Vector2(1,0);
            Vector2 bottom_left =   new Vector2(0,0);
            Vector2 bottom_rigth =  new Vector2(0,1);
            verts = new Vector2[]{top_rigth,top_left,bottom_left,bottom_rigth};

            tris = new short[]{0,1,2,0,2,3};
        }

        { // mesh stuff...
            new_verts = new float[verts.length*COORDS_PER_VERTEX];
            for(int i = 0; i<verts.length;i++)
            {
                new_verts[i*COORDS_PER_VERTEX]   = verts[i].x;
                new_verts[i*COORDS_PER_VERTEX+1] = verts[i].y;
                new_verts[i*COORDS_PER_VERTEX+2] = 0;
            }
        }
        num= value;
    }

    // called back from the gl thread by the renderer for init
    public void gl_init()
    {
        if(tris.length%3 != 0){
            throw new IllegalArgumentException("A triangle array needs to be divisible by three");
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(
                new_verts.length * 4); // float 4 bytes
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(new_verts);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(
                tris.length * 2); //short 2 bytes
        dlb.order(ByteOrder.nativeOrder());//convert to the correct winding order
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(tris);
        drawListBuffer.position(0);

        int vertexShader    = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader  = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] projectionMatrix) {
        float[] matrix = new float[16];
        Matrix.multiplyMM(matrix, 0, projectionMatrix, 0, modelMatrix, 0);

        if(num==-1)return;
        if(textures[num]==-1) {
            textures[num] = genTexture(Integer.toString(num));
        }
        GLES20.glUseProgram(mProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int texHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[num]);
        GLES20.glUniform1i(texHandle, 0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");


        mColorHandle = GLES20.glGetUniformLocation(mProgram, "u_Color");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, tris.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
    private float[] color= new float[]{0.3f,0.5f,0.5f,1f};
    public void setColor(float[] color) {
        this.color = color;
    }

    public static int genTexture(String s)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final int FONT_SIZE = 200;
            // Read in the resource
            final Bitmap bitmap = Bitmap.createBitmap(512,512,Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(0x00ffffff);
            Canvas canvas = new Canvas(bitmap);
            TextPaint textPaint = new TextPaint();
            textPaint.setTextSize(FONT_SIZE);
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));

            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(10);
            textPaint.setColor(0x33000000);
            canvas.drawText(s, xPos, yPos, textPaint);

            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(7);
            textPaint.setColor(0xff000000);
            canvas.drawText(s, xPos, yPos, textPaint);

            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(0xffffffff);
            canvas.drawText(s, xPos, yPos, textPaint);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    @Override
    public void setPos(Vector3 pos)
    {
        super.setPos(Vector3.Sub(pos,new Vector3(0.5f,0.5f,0)));
    }
}

