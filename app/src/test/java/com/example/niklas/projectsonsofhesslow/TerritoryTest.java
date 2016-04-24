package com.example.niklas.projectsonsofhesslow;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.*;

public class TerritoryTest {
    @Test
    public void testNeighbourSymmetry() throws Exception {
        Risk risk = new Risk(4);
        ArrayList<Territory> territories = new ArrayList<>(Arrays.asList(risk.getTerritories()));

        //null check should be removed, when risk constructor is done
        for(Territory territory: territories){
            if(territory.getNeighbours() != null) { //remove
                for (Territory neighbour : territory.getNeighbours()) {
                    ArrayList<Territory> neighboursList = new ArrayList<Territory>(Arrays.asList(neighbour.getNeighbours()));
                    assertTrue(neighboursList.contains(territory));
                }
            }
        }
    }
}
