package de.jozelot.stoneuniverse.mechanics.giveaway;

public enum GiveawayEnterError {
    SUCCESS(""),
    FULL("Das Giveaway ist bereits voll"),
    ALREAD_IN("Du nimmst schon an diesem Giveaway teil"),
    ENDED("Das Giveaway wurde bereits ausgelost");

    private final String text;

    GiveawayEnterError(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
