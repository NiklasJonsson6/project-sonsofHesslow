package Graphics;

import android.os.Handler;
import android.os.SystemClock;

import com.sonsofhesslow.games.risk.MainActivity;
import com.sonsofhesslow.games.risk.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import Graphics.Geometry.Vector3;
import Graphics.GraphicsObjects.*;
import Graphics.GraphicsObjects.Number;

/**
 * Created by Daniel on 11/04/2016.
 */
public class GraphicsManager {

    public static FilledBeizierPath[] beiziers;
    public static Integer[][] beizNeighbors;
    public static Integer[] beizContinents;
    public static Number[] numbers;

    public interface Updatable
    {
        boolean update(float dt);
    }

    static ConcurrentLinkedQueue<Updatable> updatables = new ConcurrentLinkedQueue<>();
    public static void init()
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            long last;
            @Override
            public void run() {
                long current = SystemClock.elapsedRealtime();
                float dt = current-last;
                boolean reqRender=false;
                for(Updatable updatable : updatables) {
                    if(updatable.update(dt)) reqRender = true;
                }
                if(reqRender) MyGLSurfaceView.ref.requestRender();
                handler.postDelayed(this, 16);
                last = current;
            }
        },16);

        try
        {
            List<SvgImporter.SVG_ReturnValue> tmp = SvgImporter.read(MainActivity.resources.openRawResource(R.raw.new_world));
            GraphicsManager.beiziers = new FilledBeizierPath[tmp.size()];
            GraphicsManager.beizNeighbors = new Integer[tmp.size()][];
            GraphicsManager.beizContinents = new Integer[tmp.size()];
            int c = 0;
            for(SvgImporter.SVG_ReturnValue ret : tmp)
            {
                GraphicsManager.beiziers[c] = ret.path;
                GraphicsManager.beizNeighbors[c] = ret.neighbors.toArray(new Integer[ret.neighbors.size()]);
                GraphicsManager.beizContinents[c] = ret.continent_id;
                updatables.add(ret.path);
                ++c;
            }

            numbers = new Number[tmp.size()];
            for(int i = 0; i<numbers.length;i++)
            {
                numbers[i] = new Number(-1);
                numbers[i].setPos(beiziers[i].getCenter());
                numbers[i].drawOrder = 1000;
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(ex.toString());
        }
    }

    public static void setHeight(int regionId, float height)
    {
        beiziers[regionId].setPos(new Vector3(0, 0, -height));
    }
    public static void setColor(int regionId, float[] Color)
    {
        beiziers[regionId].setColor(Color);
    }

    public static void setColor(int regionId, float[] Color, int originId)
    {
        beiziers[regionId].setColor(Color,beiziers[originId].getCenter());
    }

    public static void setOutlineColor(int regionId, float[] Color)
    {
        beiziers[regionId].setColorOutline(Color);
    }

    public static void setTroops(int regionId, int numberOfTroups)
    {
        numbers[regionId].setValue(numberOfTroups);
    }

    public static Integer[] getNeighbours(int regionId)
    {
        return beizNeighbors[regionId];
    }

    public static Integer getContinetId(int regionId)
    {
        return beizContinents[regionId];
    }

    public static void setTroupColor(int regionId, float[] color) {
        numbers[regionId].setColor(color);
    }

    public static Integer[] getContinentRegions(int continentId)
    {
        List<Integer> regions_in_continent = new ArrayList<>();
        int c= 0;

        for(Integer i : beizContinents)
        {
            if(continentId == i) regions_in_continent.add(c);
            ++c;
        }
        return regions_in_continent.toArray(new Integer[regions_in_continent.size()]);
    }

    public static int getNumberOfTerritories() {
        return beiziers.length;
    }
    public static void requestRender(){MyGLSurfaceView.ref.requestRender();}
}























