package com.example.niklas.projectsonsofhesslow;
import java.util.Random;

public class Die {
    public int roll(int xCordinate, int yCordinate){
        Random random = new Random();
        int number = random.nextInt(6) + 1;
        playAnimation(number, xCordinate, yCordinate);
        
        return number;
    }

    private void playAnimation(int number, int xCordinate, int yCordinate){
        //code for animation at cordinates x, y
    }
}
