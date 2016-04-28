package Graphics.GraphicsObjects;

import java.util.ArrayList;
import java.util.List;

import Graphics.Geometry.Vector2;
import Graphics.Geometry.Vector3;

/**
 * Created by Daniel on 27/04/2016.
 */
public class Number
{
    //highly temporary numbers display.
    static int c = 0;
    public Number(int value, Vector2 pos)
    {
        float scale = 0.07f;
        for(int i = 0; i<15;i++)
        {
            Vector2 sq_pos = Vector2.Add(Vector2.Mul(new Vector2(-1 + (14 - i) % 3, -2 + (14 - i) / 3), scale), pos);
            squares[i] = new Square(sq_pos,scale,new float[]{0,0,0,1});
            squares[i].isActive = false;
            squares[i].drawOrder = 1000;
        }
    }
    //temporary
    private int value;
    int[] zero= {1,1,1,1,0,1,1,0,1,1,0,1,1,1,1};
    int[] one = {0,0,1,0,0,1,0,0,1,0,0,1,0,0,1};
    int[] two=  {1,1,1,0,0,1,1,1,1,1,0,0,1,1,1};
    int[] three={1,1,1,0,0,1,1,1,1,0,0,1,1,1,1};
    int[] four= {1,0,1,1,0,1,1,1,1,0,0,1,0,0,1};
    int[] five= {1,1,1,1,0,0,1,1,1,0,0,1,1,1,1};
    int[] six=  {1,1,1,1,0,0,1,1,1,1,0,1,1,1,1};
    int[] seven={1,1,1,0,0,1,0,0,1,0,0,1,0,0,1};
    int[] eight={1,1,1,1,0,1,1,1,1,1,0,1,1,1,1};
    int[] nine= {1,1,1,1,0,1,1,1,1,0,0,1,1,1,1};
    int[][] values = {zero,one,two,three,four,five,six,seven,eight,nine};

    Square[] squares = new Square[15];
    public void setValue(int value) {
        int[] set = values[value%10];
        for(int i = 0; i<set.length;i++)
        {
            squares[i].isActive = set[i]==1;
        }
    }


}
