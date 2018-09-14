package com.github.rubenwilhelmsen.virtualtreasurehunting;

import com.google.android.gms.maps.model.LatLng;

public class Game {

    private Treasure[] treasures;
    private String gamemode;
    private int maxDistance;


    public Game(Treasure[] treasures, int maxDistance, String gamemode) {
        this.treasures = treasures;
        this.maxDistance = maxDistance;
        this.gamemode = gamemode;
    }


    public Treasure[] getTreasures() {
        return treasures;
    }

    /**
     * Marks a {@code Treasure} as opened in the {@code treasures} array.
     * @param position position of {@code Treasure} to mark
     */
    public void openTreasure(LatLng position) {
        for (int i = 0; i < treasures.length; i++) {
            if (treasures[i].getPosition().equals(position)) {
                treasures[i].setOpened(true);
            }
        }
    }

    /**
     * Replaces a {@code Treasure}.
     * @param position position of treasure to be replaced
     * @param treasure {@code Treasure} to replace with
     */
    public void replaceTreasure(LatLng position, Treasure treasure) {
        for (int i = 0; i < treasures.length; i++) {
            if (treasures[i].getPosition().equals(position)) {
                treasures[i] = treasure;
            }
        }
    }

    /**
     * Checks if game is finished.
     * @return true if all treasures have been opened
     */
    public boolean gameFinished() {
        for (int i = 0; i < treasures.length; i++) {
            if (!treasures[i].getOpened()) {
                return false;
            }
        }
        return true;
    }

    public String getGamemode() {
        return gamemode;
    }

    public int getMaxDistance() {
        return maxDistance;
    }
}
