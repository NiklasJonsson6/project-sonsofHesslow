package Graphics;

import java.util.ArrayList;
import java.util.List;

import Graphics.GraphicsObjects.FilledBeizierPath;

/**
 * Created by Daniel on 11/04/2016.
 */
public class GraphicsManager {

    public static FilledBeizierPath[] beiziers;
    public static Integer[][] beizNeighbors;
    public static Integer[] beizContinents;

    public static void setColor(int regionId, float[] Color)
    {
        beiziers[regionId].mesh.color = Color;
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
