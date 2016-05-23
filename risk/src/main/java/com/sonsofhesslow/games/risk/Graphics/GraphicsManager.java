package com.sonsofhesslow.games.risk.graphics;

import android.content.res.Resources;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Pair;

import com.sonsofhesslow.games.risk.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sonsofhesslow.games.risk.graphics.Geometry.Vector3;
import com.sonsofhesslow.games.risk.graphics.GraphicsObjects.*;
import com.sonsofhesslow.games.risk.graphics.GraphicsObjects.Number;

public class GraphicsManager {

    FilledBeizierPath[] beiziers;
    private Integer[][] beizNeighbors;
    private Integer[] beizContinents;
    private Number[] numbers;

    static GraphicsManager instance;
    public static GraphicsManager getInstance()
    {
        if(instance == null) instance = new GraphicsManager();
        return instance;
    }

    private final ConcurrentLinkedQueue<Updatable> updatables = new ConcurrentLinkedQueue<>();
    private Renderer renderer;
    public void init(Resources resources, Renderer renderer)
    {
        this.renderer = renderer;
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
            List<SvgImporter.SVG_ReturnValue> tmp = SvgImporter.read(resources.openRawResource(R.raw.new_world),renderer);
            beiziers = new FilledBeizierPath[tmp.size()];
            beizNeighbors = new Integer[tmp.size()][];
            beizContinents = new Integer[tmp.size()];
            int c = 0;
            for(SvgImporter.SVG_ReturnValue ret : tmp)
            {
                beiziers[c] = ret.path;
                beizNeighbors[c] = ret.neighbors.toArray(new Integer[ret.neighbors.size()]);
                beizContinents[c] = ret.continent_id;
                updatables.add(ret.path);
                ++c;
            }

            numbers = new Number[tmp.size()];
            for(int i = 0; i<numbers.length;i++)
            {
                numbers[i] = new Number(-1,renderer);
                numbers[i].setPos(beiziers[i].getCenter());
                numbers[i].drawOrder = 1000;
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(ex.toString());
        }
    }

    public void setHeight(int regionId, float height)
    {
        beiziers[regionId].setPos(new Vector3(0, 0, -height));
    }
    private final Map<Pair<Integer,Integer>,NumberedArrow> arrows = new HashMap<>();
    public void addArrow(int territoryIdFrom, int territoyIdTo, int value,float[] color)
    {
        arrows.put(new Pair<>(territoryIdFrom,territoyIdTo),
                new NumberedArrow(renderer,beiziers[territoryIdFrom].getCenter(),
                        beiziers[territoyIdTo].getCenter(),color,value));
    }
    public void removeArrow(int territoryIdFrom, int territoyIdTo)
    {
        Iterator<Map.Entry<Pair<Integer,Integer>,NumberedArrow>> iter = arrows.entrySet().iterator();
        while(iter.hasNext())  // because foreach loops can't handle them removes...
        {
            Map.Entry<Pair<Integer,Integer>,NumberedArrow> entry = iter.next();
            if(entry.getKey().first.equals(territoryIdFrom) && entry.getKey().second.equals(territoyIdTo))
            {
                entry.getValue().remove();
                iter.remove();
            }
        }
    }

    public void setColor(int regionId, float[] Color)
    {
        beiziers[regionId].setColor(Color);
    }

    public void setColor(int regionId, float[] Color, int originId)
    {
        beiziers[regionId].setColor(Color,beiziers[originId].getCenter());
    }

    public void setOutlineColor(int regionId, float[] Color)
    {
        beiziers[regionId].setColorOutline(Color);
    }

    public void setArmies(int regionId, int numberOfArmies)
    {
        numbers[regionId].setValue(numberOfArmies);
    }

    public Integer[] getNeighbours(int regionId)
    {
        return beizNeighbors[regionId];
    }

    public Integer getContinetId(int regionId)
    {
        return beizContinents[regionId];
    }

    public void setArmyColor(int regionId, float[] color) {
        numbers[regionId].setColor(color);
    }

    public Integer[] getContinentRegions(int continentId)
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

    public int getNumberOfTerritories() {
        return beiziers.length;
    }
    public void  requestRender(){MyGLSurfaceView.ref.requestRender();}
}























