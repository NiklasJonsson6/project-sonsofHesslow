package Graphics.GraphicsObjects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.text.TextPaint;

import com.example.niklas.projectsonsofhesslow.MainActivity;
import com.example.niklas.projectsonsofhesslow.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import Graphics.Geometry.Vector2;
import Graphics.MyGLRenderer;

/**
 * Created by Daniel on 04/05/2016.
 */
public class Text extends GLObject{
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
                    "varying vec2 u_tc;"+
                    "void main() {" +
                    "  gl_FragColor = texture2D(u_Texture, u_tc);"+
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
    public void setValue(int value)
    {
        num = value;
    }
    public Text(int value) {
        if(textures == null)
        {
            textures = new int[200];
            for(int i = 0; i<textures.length;i++)
            {
                textures[i]=-1;
            }
        }
        {
            Vector2 center = new Vector2(0,0);

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

    public void gl_init()
    {
        if(tris.length%3 != 0){
            throw new IllegalArgumentException("A triangle array needs to be divisible by three");
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                new_verts.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(new_verts);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                tris.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(tris);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader    = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader  = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public void draw(float[] projectionMatrix) {
        float[] matrix = new float[16];
        Matrix.multiplyMM(matrix, 0, projectionMatrix, 0, modelMatrix, 0);

        if(num==-1)return;
        if(textures[num]==-1) {
            textures[num] = genTexture(Integer.toString(num));
        }

        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");


        int texHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[num]);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(texHandle, 0);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, matrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, tris.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
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
            textPaint.setTextSize(FONT_SIZE );
            textPaint.setSubpixelText(true);
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));

            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(10);
            textPaint.setColor(0x33ffffff);
            canvas.drawText(s, xPos, yPos, textPaint);


            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(7);
            textPaint.setColor(0xffffffff);
            canvas.drawText(s, xPos, yPos, textPaint);

            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(0xff000000);
            canvas.drawText(s, xPos, yPos, textPaint);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
}

