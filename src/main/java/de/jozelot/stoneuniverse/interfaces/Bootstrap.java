package de.jozelot.stoneuniverse.interfaces;

public interface Bootstrap {
    boolean register();
    boolean enable();
    void shutdown();
    void reload();
}
