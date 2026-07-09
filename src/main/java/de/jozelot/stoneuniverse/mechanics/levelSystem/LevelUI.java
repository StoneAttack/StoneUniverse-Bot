package de.jozelot.stoneuniverse.mechanics.levelSystem;

import de.jozelot.stoneuniverse.StoneUniverse;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.User;

public class LevelUI {

    private final StoneUniverse bot;

    public LevelUI(StoneUniverse bot) {
        this.bot = bot;
    }

    public Container getXpDisplay(User targetUser, UserLevel userLevel) {
        return Container.of(
                TextDisplay.of("# 📊 XP Status"),
                TextDisplay.of("Hier sind die aktuellen Erfahrungspunkte von " + targetUser.getAsMention() + "."),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**XP-Stand:** `" + userLevel.getXp() + "` XP"),
                TextDisplay.of("**Fortschritt:** `" + userLevel.getXp() + "/" + userLevel.getXpNeeded() + "` XP bis zum nächsten Level")
        );
    }

    public Container getLevelDisplay(User targetUser, UserLevel userLevel, int rank) {
        return Container.of(
                TextDisplay.of("# 🏆 Level Status"),
                TextDisplay.of("Hier ist das aktuelle Level von " + targetUser.getAsMention() + "."),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**Level:** `" + userLevel.getLevel() + "`"),
                TextDisplay.of("**Server Platzierung:** `" + (rank == -1 ? "Unbekannt" : "#" + rank) + "`")
        );
    }

    public Container getXpChangedSuccess(User targetUser, String action, int amount, int currentLevel) {
        String details = switch (action.toLowerCase()) {
            case "set" -> "Die XP wurden fest auf **" + amount + "** gesetzt.";
            case "add" -> "Es wurden erfolgreich **" + amount + "** XP hinzugefügt.";
            case "remove" -> "Es wurden erfolgreich **" + amount + "** XP abgezogen.";
            default -> "Die XP wurden modifiziert.";
        };

        return Container.of(
                TextDisplay.of("# ✅ XP Aktualisiert"),
                TextDisplay.of("Die Administration hat die Profilwerte von " + targetUser.getAsMention() + " angepasst."),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**Aktion:** " + details),
                TextDisplay.of("**Resultierendes Level:** `" + currentLevel + "`")
        );
    }

    public Container getLevelChangedSuccess(User targetUser, String action, int amount, int currentLevel) {
        String details = switch (action.toLowerCase()) {
            case "set" -> "Das Level wurde fest auf **" + amount + "** gesetzt.";
            case "add" -> "Es wurden erfolgreich **" + amount + "** Level hinzugefügt.";
            case "remove" -> "Es wurden erfolgreich **" + amount + "** Level abgezogen.";
            default -> "Das Level wurde modifiziert.";
        };

        return Container.of(
                TextDisplay.of("# ✅ Level Aktualisiert"),
                TextDisplay.of("Die Administration hat die Profilwerte von " + targetUser.getAsMention() + " angepasst."),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**Aktion:** " + details),
                TextDisplay.of("**Neues Level:** `" + currentLevel + "`")
        );
    }
}