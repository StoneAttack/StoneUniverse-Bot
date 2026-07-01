package de.jozelot.stoneuniverse.util;

import de.jozelot.stoneuniverse.data.hosts.HostsManager;
import de.jozelot.stoneuniverse.mechanics.CountingSystem;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Messages {

    public static Container getConnectionInfo(HostsManager hosts) {
        /*LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy  •  HH:mm 'Uhr'");
        String formatted = now.format(formatter);*/

        return Container.of(
                TextDisplay.of("# So verbindest du dich!"),
                TextDisplay.of("-# Du kannst auf **Stone Universe** über die Java und Bedrock Edition spielen."),

                Separator.createDivider(Separator.Spacing.LARGE),

                TextDisplay.of("### Java"),
                TextDisplay.of("IP-Addresse: ```" + hosts.getJava().getHostname() + "```"),
                TextDisplay.of("Port (Freilassen oder): ```" + hosts.getJava().getPort() + "```"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("### Bedrock"),
                TextDisplay.of("IP-Addresse: ```" + hosts.getBedrock().getHostname() + "```"),
                TextDisplay.of("Port: ```" + hosts.getJava().getPort() + "```"),

                Separator.createDivider(Separator.Spacing.LARGE),

                TextDisplay.of("-# " + hosts.getStand())

                /*,ActionRow.of(
                        Button.of(ButtonStyle.SECONDARY, "ip_embed:okay", "Okay", Emoji.fromUnicode("☑"))
                )*/
        );
    }

    public static Container getCountingFailed(User user, int count, boolean isDouble, boolean isHighscore) {
        String reason = "-# Du darfst nicht 2 Zahlen hintereinander schreiben!";
        if (!isDouble) {
            reason = "-# Falsche Zahl! Es wäre **" + (count + 1) + "** gewesen.";
        }

        if (isHighscore) {
            return Container.of(
                    TextDisplay.of("# \uD83C\uDFC6 Neuer Highscore!"),
                    TextDisplay.of("In dieser Runde wurde die Bestmarke auf **" + count + "** angehoben!"),

                    Separator.createDivider(Separator.Spacing.SMALL),

                    TextDisplay.of("### ⛔ Runde vorbei"),
                    TextDisplay.of(user.getAsMention() + " hat die Counting Runde bei **" + count + "** zerstört."),
                    TextDisplay.of(reason),

                    Separator.createDivider(Separator.Spacing.SMALL),

                    TextDisplay.of("Die nächste Zahl ist **1**."),

                    ActionRow.of(
                            Button.of(ButtonStyle.PRIMARY, "counting:stats", "Stats" ,Emoji.fromUnicode("\uD83D\uDCCA"))
                            //Button.of(ButtonStyle.SECONDARY, "counting:info", "Reaktionen", Emoji.fromUnicode("☑\uFE0F"))
                    )
            );
        }

        return Container.of(
                TextDisplay.of("# ⛔ Runde verloren"),
                TextDisplay.of(user.getAsMention() + " hat die Counting Runde bei **" + count + "** zerstört!"),
                TextDisplay.of(reason),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("Die nächste Zahl ist **1**."),

                ActionRow.of(
                        Button.of(ButtonStyle.PRIMARY, "counting:stats", "Stats" ,Emoji.fromUnicode("\uD83D\uDCCA"))
                        //Button.of(ButtonStyle.SECONDARY, "counting:info", "Reaktionen", Emoji.fromUnicode("☑\uFE0F"))
                )
        );
    }

    public static Container getCountingInfo(int count, User lastCounter) {
        String counterInfo = "";
        if (lastCounter != null) counterInfo = "-# " + lastCounter.getAsMention() + " hat als letztes gezählt und muss einmal warten!";
        return Container.of(
                TextDisplay.of("# \uD83D\uDD01 Counting Fortsetzung"),
                TextDisplay.of("Der Bot ist wieder online! Während der Downtime konnte nicht gezählt werden.\n" + counterInfo),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("Die nächste Zahl ist **" + count + "**."),

                ActionRow.of(
                        Button.of(ButtonStyle.PRIMARY, "counting:stats", "Stats" ,Emoji.fromUnicode("\uD83D\uDCCA"))
                        //Button.of(ButtonStyle.SECONDARY, "counting:info", "Reaktionen", Emoji.fromUnicode("☑\uFE0F"))
                )
        );
    }

    public static Container getCountingStats(CountingSystem cs) {
        int currentCount = cs.getCurrentCount();
        int highscore = cs.getCurrentHighscore();
        int roundsPlayed = cs.getRoundsPlayed();

        return Container.of(
                TextDisplay.of("# \uD83D\uDCCA Counting Statistiken"),
                TextDisplay.of("-# Hier siehst du den aktuellen Stand des Minigames."),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("\uD83D\uDD25 **Aktuelle Zahl:** `" + currentCount + "`"),
                TextDisplay.of("\uD83C\uDFC6 **Höchster Rekord:** `" + highscore + "`"),
                TextDisplay.of("\uD83D\uDD04 **Gespielte Runden:** `" + roundsPlayed + "`")
        );
    }

    public static Container getLevelUp(Member member, int newLevel, int newXp) {
        return Container.of(
                TextDisplay.of("# \uD83C\uDFC6 Level Up"),
                TextDisplay.of(member.getAsMention() + " ist ein Level aufgestiegen. Weiter so!"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("\uD83D\uDD25 **Neues Level:** `" + newLevel + "` Level"),
                TextDisplay.of("\uD83C\uDFC6 **Neuer XP-Stand:** `" + newXp + "` XP"),

                ActionRow.of(
                        Button.of(ButtonStyle.PRIMARY, "level:leaderboard", "Leaderboard", Emoji.fromUnicode("\uD83E\uDDEE"))
                )
        );
    }
}
