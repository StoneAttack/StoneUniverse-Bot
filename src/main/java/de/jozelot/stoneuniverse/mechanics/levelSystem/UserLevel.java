package de.jozelot.stoneuniverse.mechanics.levelSystem;

import java.util.concurrent.ThreadLocalRandom;

public class UserLevel {

    private final long userId;
    private int xp;
    private int level;
    private long lastXpGain;

    public UserLevel(long userId, int xp, int level) {
        this.userId = userId;
        this.xp = xp;
        this.level = level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void addLevel(int amount) {
        level += amount;
    }

    public void removeLevel(int amount) {
        level -= amount;
    }


    public boolean addMessageXp() {
        // Random XP schub
        int xpGained = ThreadLocalRandom.current().nextInt(15, 26);
        xp += xpGained;
        return checkLevel();
    }

    public boolean checkLevel() {
        int xpNeeded = 5 * (level * level) + (50 * level) + 100;
        boolean leveledUp = false;

        // Multi Level XP Möglich
        while (this.xp >= xpNeeded) {
            this.xp -= xpNeeded;
            this.level++;
            leveledUp = true;

            xpNeeded = 5 * (level * level) + (50 * level) + 100;
        }

        return leveledUp;
    }

    public long getUserId() {
        return userId;
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public void setLastXpGain(long xpGain) {
        this.lastXpGain = xpGain;
    }

    public long getLastXpGain() {
        return lastXpGain;
    }
}
