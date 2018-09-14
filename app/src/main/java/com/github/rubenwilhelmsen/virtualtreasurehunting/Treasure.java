package com.github.rubenwilhelmsen.virtualtreasurehunting;

import com.google.android.gms.maps.model.LatLng;

public class Treasure {

    private LatLng position;
    private boolean opened;

    /**
     * Constructor used when creating a new game.
     * @param position the treasures position
     */
    public Treasure(LatLng position) {
        this.position = position;
        opened = false;
    }

    /**
     * Constructor used when loading a game.
     * @param position the treasures position
     * @param opened true if the treasure has been opened
     */
    public Treasure(LatLng position, boolean opened) {
        this.position = position;
        this.opened = opened;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setOpened(boolean b) {
        opened = b;
    }

    public boolean getOpened() {
        return opened;
    }


}
