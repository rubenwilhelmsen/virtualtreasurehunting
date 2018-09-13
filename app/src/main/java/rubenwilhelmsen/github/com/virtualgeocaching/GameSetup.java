package rubenwilhelmsen.github.com.virtualgeocaching;


import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

//Hanterar skapandet av ett nytt spel.
public class GameSetup {

    private int numberOfTreasures;
    private int maxDistance;
    private Treasure[] treasures;
    private Treasure newTreasure;
    private LatLng userPosition;

    /**
     * Constructor used when setting up a new game.
     * @param numberOfTreasures number of treasure
     * @param maxDistance treasure will not be further away than this
     * @param userPosition the users position
     */
    public GameSetup(int numberOfTreasures, int maxDistance, LatLng userPosition) {
        this.numberOfTreasures = numberOfTreasures;
        this.maxDistance = maxDistance;
        this.userPosition = userPosition;
        treasures = new Treasure[numberOfTreasures];
        if (!calculateTreasures()) {
            treasures = null;
        }
    }

    //Används vid utbyte av skatt
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

    //Skapar listan med skatter som spelet kommer använda sig av.
    /**
     * Generates a number of treasures (according to {@code numberOfTreasures}) and inserts them into the {@code treasures} array.
     */
    private boolean calculateTreasures() {
        for (int i = 0; i < numberOfTreasures; i++) {
            Treasure temp;
            temp = new Treasure(calculatePosition(getRandomDirection(), getRandomDistance()));
            if (temp != null) {
                treasures[i] = temp;
            } else {
                return false;
            }
        }
        return true;
    }

    //Integer:en returnerat av metoden representerar ett vädersträck. 0-7 är: N, NO, O, SO, S, SV, V, NV (i samma ordning).
    /**
     * Generates a random integer which represents a point on a compass.
     * @return 0-7, represents N, NE, E, SE, S, SW, W, NW (in that order)
     */
    private int getRandomDirection() {
        Random temp = new Random();
        return temp.nextInt(8);
    }

    //Returnerar en slumpmässig integer beroende på vad användaren valde för maxDistance.
    /**
     * Generates a random double less than {@code maxDistance}.
     * @return the random distance
     */
    private double getRandomDistance() {
        Random temp = new Random();
        return (250 + temp.nextInt(maxDistance)) * 0.001;
    }

    //Skapar koordinater till en skatt genom den slumpade riktingen samt avståndet.
    //kilometer ska motsvara lite mindre än en kilometer i koordinat-grader. 1 grad är 111km vid ekvatorn och därmed blir 1km 0.009009009 och 0.006500009000009 lite mindre än en km.
    //Detta blir dock mindre precist ju kängre ifrån ekvatorn man befinner sig.
    //accountForMiddle används i "mellan-väderstrecken" (NO,SO,SV,NV) för att minska avståndet lite eftersom där adderas både lat och lang.
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
}
