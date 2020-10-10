/******************************************************************************

 Project     : CMP-5015Y - Java Programming:
               Java_Card_Game.

 File        : StrategyFactory.java

 Date        : Tuesday 04 February 2020

 Author      : Martin Siddons

 Description : Simple piece of code used by BasicCheat to take a name in a
 string and return a Strategy.

 History     : 04/02/2020 - v1.0 - Initial setup, 
 ******************************************************************************/
package com.question2;

import java.util.Random;

public class StrategyFactory {

    public static boolean set(Player p, String s) {
        s = s.toLowerCase();
        s = s.replaceAll("[^a-z]+", ""); // Keep only letters

        switch (s) {
            case "basic":
                p.setStrategy(new BasicStrategy());
                return true;
            case "thinker":
                p.setStrategy(new ThinkerStrategy());
                return true;
            case "master":
                p.setStrategy(new MyStrategy());
                return true;
            case "human":
                p.setStrategy(new HumanStrategy());
                return true;
            case "random":
                Random rand = new Random();
                String[] r = new String[3];
                r[0] = "basic";
                r[1] = "thinker";
                r[2] = "master";
                String random = r[rand.nextInt(3)];
                System.out.println("Setting player to " + random + ".");
                return set(p, random);
            default:
                System.out.println("Error: Strategy " + s + " not found.");
                return false;
        }
    }
}
