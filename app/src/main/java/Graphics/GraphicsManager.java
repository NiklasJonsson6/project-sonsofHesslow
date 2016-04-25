package Graphics;

import android.graphics.Region;

import com.example.niklas.projectsonsofhesslow.MainActivity;
import com.example.niklas.projectsonsofhesslow.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Graphics.Geometry.Vector3;
import Graphics.GraphicsObjects.FilledBeizierPath;
import Graphics.GraphicsObjects.GLObject;

/**
 * Created by Daniel on 11/04/2016.
 */
public class GraphicsManager {

    public static FilledBeizierPath[] beiziers;
    public static Integer[][] beizNeighbors;
    public static Integer[] beizContinents;

    public static void init()
    {
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
                ++c;
            }

        } catch (IOException ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(ex.toString());
        }
    }

    public static void setHeight(int regionId, float height)
    {
        beiziers[regionId].setPos(new Vector3(0,0,-height));
    }
    public static void setColor(int regionId, float[] Color)
    {
        beiziers[regionId].fill_mesh.color = Color;
    }

    public static void setOutlineColor(int regionId, float[] Color)
    {
        beiziers[regionId].outline_mesh.color = Color;
    }

    public static void setTroops(int regionId, int numberOfTroups)
    {
        throw new RuntimeException("not yet implemented");
    }

    public static Integer[] getNeighbours(int regionId)
    {
        return beizNeighbors[regionId];
    }

    public static Integer getContinetId(int regionId)
    {
        return beizContinents[regionId];
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
}























