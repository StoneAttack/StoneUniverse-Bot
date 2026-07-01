package de.jozelot.stoneuniverse.data.config;

public enum ActivityState {
    ONLINE,
    MAINTENANCE,
    OFFLINE;

    public static ActivityState fromString(String state) {
        try {
            return ActivityState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OFFLINE;
        }
    }
}
