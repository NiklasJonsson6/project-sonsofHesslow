package com.sonsofhesslow.games.risk.graphics.GraphicsObjects;

import android.opengl.Matrix;

import com.sonsofhesslow.games.risk.graphics.Geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.utils.MathsUtil;

/**
 * Created by Daniel on 12/05/2016.
 */
public class NumberedArrow {
    private Number number;
    private Arrow arrow;

    final float PI = (float) Math.PI;
    public NumberedArrow(Renderer renderer, Vector2 from, Vector2 to, float[] color, int value)
    {
        number = new Number(value,renderer,color);
        Vector2 delta = Vector2.Sub(to,from);
        float angle = 0;
        float scale = 0.3f;
        float[] transMatrix= new float[16];
        Vector2 pos = MathsUtil.lerp(from, to, 0.5f);
        Matrix.translateM(transMatrix, 0, number.getMatrix(), 0, pos.x-0.5f*scale, pos.y-0.5f*scale, 0);

        float[] rotMatrix = new float[16];
        Matrix.rotateM(rotMatrix,0,transMatrix,0,(float)Math.toDegrees(angle),0,0,1);

        Matrix.scaleM(number.getMatrix(), 0, rotMatrix, 0, scale, scale, scale);

        arrow = new Arrow(from,to, color, renderer);
        arrow.drawOrder = 10000;
        number.drawOrder = 10001;
    }
    public void remove()
    {
        number.Remove();
        arrow.Remove();
    }
    public void setValue(int value){number.setValue(value);};
    public int  getValue(){return number.getValue();}
}
