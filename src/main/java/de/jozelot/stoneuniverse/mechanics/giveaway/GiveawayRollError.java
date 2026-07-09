package de.jozelot.stoneuniverse.mechanics.giveaway;

public enum GiveawayRollError {
    NO_ENTRIES("Es gibt keine Teilnehmer!"),
    ALREADY_ENDED("Das Giveaway wurde schon ausgelost.");

    private final String text;

    GiveawayRollError(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
