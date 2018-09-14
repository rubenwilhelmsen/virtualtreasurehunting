package com.github.rubenwilhelmsen.virtualtreasurehunting;

import java.util.Random;

public class MinigameLogic {

    private int completedActions;
    private int lastRandom;

    public MinigameLogic() {
        completedActions = 0;
        lastRandom = -1;
    }

    public int actionCompleted() {
        return ++completedActions;

    }

    public boolean minigameCompleted() {
        return completedActions == 3;
    }

    /**
     * Generates a random integer between 0-3. Represents a card to be shown during the minigame. Does not generate the same integer twice in a row. Also does not generate 0 if the last integer generated was 2 and vice versa.
     * @return the random integer
     */
    public int getRandomAction() {
        Random temp = new Random();
        int i = temp.nextInt(4);
        if ((i == lastRandom) || (lastRandom == 0 && i == 2) || (lastRandom == 2 && i == 0)) {
            return getRandomAction();
        } else {
            lastRandom = i;
            return i;
        }
    }
}
