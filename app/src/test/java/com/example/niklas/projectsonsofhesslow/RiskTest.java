package com.example.niklas.projectsonsofhesslow;

import org.junit.Test;

import static org.junit.Assert.*;

public class RiskTest {
    @Test
    public void testPlayerCreation() throws Exception {
        Risk risk = new Risk(3);
        assertNotNull(risk.getPlayers());
        assertEquals(3, risk.getPlayers().length);
    }
}
