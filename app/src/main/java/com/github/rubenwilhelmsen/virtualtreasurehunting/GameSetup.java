package com.github.rubenwilhelmsen.virtualtreasurehunting;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

public class GameSetup implements Parcelable {

    private int numberOfTreasures, maxDistance, timeLimit;
    private Treasure[] treasures;
    private Treasure newTreasure;
    private LatLng userPosition;
    private String gameMode;

    /**
     * Constructor used when setting up a new game.
     * @param numberOfTreasures number of treasure
     * @param maxDistance treasure will not be further away than this
     */
    public GameSetup(int numberOfTreasures, int maxDistance, int timeLimit, String gameMode) {
        this.numberOfTreasures = numberOfTreasures;
        this.maxDistance = maxDistance;
        this.timeLimit = timeLimit;
        this.gameMode = gameMode;
        treasures = new Treasure[numberOfTreasures];
    }

    /**
     * Constructor used when replacing a treasure.
     * @param maxDistance treasure will not be further away than this
     * @param userPosition the users position
     */
    public GameSetup(int maxDistance, LatLng userPosition) {
        this.maxDistance = maxDistance;
        this.userPosition = userPosition;
        newTreasure = new Treasure(calculatePosition(getRandomDirection(), getRandomDistance()));
    }

    protected GameSetup(Parcel in) {
        numberOfTreasures = in.readInt();
        maxDistance = in.readInt();
        timeLimit = in.readInt();
        userPosition = in.readParcelable(LatLng.class.getClassLoader());
        gameMode = in.readString();
    }

    public static final Creator<GameSetup> CREATOR = new Creator<GameSetup>() {
        @Override
        public GameSetup createFromParcel(Parcel in) {
            return new GameSetup(in);
        }

        @Override
        public GameSetup[] newArray(int size) {
            return new GameSetup[size];
        }
    };

    /**
     * Generates a number of treasures (according to {@code numberOfTreasures}) and inserts them into the {@code treasures} array.
     */
    public boolean calculateTreasures(LatLng position) {
        userPosition = position;
        for (int i = 0; i < numberOfTreasures; i++) {
            Treasure temp = null;
            LatLng pos = calculatePosition(getRandomDirection(), getRandomDistance());
            if (pos != null) {
                temp = new Treasure(pos);
            }
            if (temp != null) {
                treasures[i] = temp;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Generates a random integer which represents a point on a compass.
     * @return 0-7, represents N, NE, E, SE, S, SW, W, NW (in that order)
     */
    private int getRandomDirection() {
        Random temp = new Random();
        return temp.nextInt(8);
    }

    /**
     * Generates a random double less than {@code maxDistance}.
     * @return the random distance
     */
    private double getRandomDistance() {
        Random temp = new Random();
        return (250 + temp.nextInt(maxDistance)) * 0.001;
    }

    /**
     * Generates a {@code LatLng} based on {@code getRandomDirection} and {@code getRandomDistance}.
     * @param direction a random direction, should be returned by {@code getRandomDirection}
     * @param distance a random distance, should be returned by {@code getRandomDistance}
     * @return the coordinates generated
     */
    private LatLng calculatePosition(int direction, double distance) {
        if (userPosition != null) {
            double kilometer = 0.006500009000009;
            double coordinateChange = distance * kilometer;
            double accountForMiddle = 0.002;
            LatLng coordinates = new LatLng(0, 0);

            switch (direction) {
                case 0:
                    coordinates = new LatLng(userPosition.latitude + coordinateChange, userPosition.longitude);
                    break;
                case 1:
                    coordinates = new LatLng(userPosition.latitude + coordinateChange - accountForMiddle, userPosition.longitude + coordinateChange - accountForMiddle);
                    break;
                case 2:
                    coordinates = new LatLng(userPosition.latitude, userPosition.longitude + coordinateChange);
                    break;
                case 3:
                    coordinates = new LatLng(userPosition.latitude - coordinateChange + accountForMiddle, userPosition.longitude + coordinateChange - accountForMiddle);
                    break;
                case 4:
                    coordinates = new LatLng(userPosition.latitude - coordinateChange, userPosition.longitude);
                    break;
                case 5:
                    coordinates = new LatLng(userPosition.latitude - coordinateChange + accountForMiddle, userPosition.longitude - coordinateChange + accountForMiddle);
                    break;
                case 6:
                    coordinates = new LatLng(userPosition.latitude, userPosition.longitude - coordinateChange);
                    break;
                case 7:
                    coordinates = new LatLng(userPosition.latitude + coordinateChange - accountForMiddle, userPosition.longitude - coordinateChange + accountForMiddle);
                    break;
            }
            return coordinates;
        } else {
            return null;
        }
    }

    public Treasure[] getTreasures() {
        return treasures;
    }

    public Treasure getNewTreasure() {
        return newTreasure;
    }

    public String getGameMode() {
        return gameMode;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(numberOfTreasures);
        parcel.writeInt(maxDistance);
        parcel.writeInt(timeLimit);
        parcel.writeParcelable(userPosition, i);
        parcel.writeString(gameMode);
    }
}
