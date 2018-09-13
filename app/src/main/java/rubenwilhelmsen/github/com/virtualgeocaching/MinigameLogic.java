package rubenwilhelmsen.github.com.virtualgeocaching;

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

    //Returnerar en slumpad integer, integern används för att bestämma vilken minigame-kort som ska visas (se OpenTreasureMinigameActivity metod setupAction()).
    //Ifall nextInt returnerar samma integer som förra gången den anropats slumpas en annan, samt ifall det förra kortet var shake (0) och värdet som slumpades blev hoppa(2) och tvärtom.
    //Detta är pga att ifall man får skaka efter ett hopp är det stor chans att den automatiskt avklaras eftersom den hinner byta innan man landat samt ifall man skakar och får därefter hopp finns det möjlighet att den registrerar den förra skakningen som ett hopp och även där avklaras.
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
