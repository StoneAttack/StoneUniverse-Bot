package de.jozelot.stoneuniverse.util;

import de.jozelot.stoneuniverse.data.hosts.HostsManager;
import de.jozelot.stoneuniverse.mechanics.CountingSystem;
import de.jozelot.stoneuniverse.mechanics.levelSystem.UserLevel;
import net.dv8tion.jda.api.components.Component;
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
import java.util.List;

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
                TextDisplay.of("IP-Adresse: ```" + hosts.getJava().getHostname() + "```"),
                TextDisplay.of("Port (Freilassen oder): ```" + hosts.getJava().getPort() + "```"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("### Bedrock"),
                TextDisplay.of("IP-Addresse: ```" + hosts.getBedrock().getHostname() + "```"),
                TextDisplay.of("Port: ```" + hosts.getBedrock().getPort() + "```"),

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
        if (lastCounter != null) counterInfo = "-# " + lastCounter.getAsMention() + " hat als letztes gezählt und muss warten!";
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

    public static Container getLevelUp(Member member, int newLevel, int newXp, int xpNeeded) {
        return Container.of(
                TextDisplay.of("# \uD83C\uDFC6 Level Up"),
                TextDisplay.of(member.getAsMention() + " ist ein Level aufgestiegen. Weiter so!"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("\uD83D\uDD25 **Neues Level:** `" + newLevel + "`"),
                TextDisplay.of("\uD83C\uDFC6 **Neuer XP-Stand:** `" + newXp + "/" + xpNeeded + "` XP"),

                ActionRow.of(
                        Button.of(ButtonStyle.PRIMARY, "level:leaderboard", "Leaderboard", Emoji.fromUnicode("\uD83E\uDDEE"))
                )
        );
    }

    public static Container getRank(Member member, int level, int xp, int xpNeeded, int rank) {
        return Container.of(
                TextDisplay.of("# \uD83D\uDCCA Rank Stats"),
                TextDisplay.of("Hier sind alle Informationen zum Aktivitätsstatus von " + member.getAsMention() + "."),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("\uD83D\uDD25 **Level:** `" + level + "`"),
                TextDisplay.of("\uD83C\uDFC6 **XP-Stand:** `" + xp + "/" + xpNeeded + "` XP"),
                TextDisplay.of("\uD83D\uDCCA **Server Rank:** " + rank + ". Platz"),

                ActionRow.of(
                        Button.of(ButtonStyle.PRIMARY, "level:leaderboard", "Leaderboard", Emoji.fromUnicode("\uD83E\uDDEE"))
                )
        );
    }

    public static Container getError(String message) {
        return Container.of(
                TextDisplay.of("# \uD83D\uDCDB Fehler!"),
                TextDisplay.of("Es ist ein unerwarteter Fehler aufgetreten!"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("```" + message +"```")
        );
    }

    public static Container getLeaderboard(List<UserLevel> levels) {
        StringBuilder stringBuilder = new StringBuilder();
        int current = 0;
        for (UserLevel level : levels) {
            current++;
            stringBuilder.append("**" + current + ".** <@" + level.getUserId() + "> Level: `" + level.getLevel() + "`\n");
        }

        String userLevels = stringBuilder.toString();

        if (userLevels.isEmpty()) {
            userLevels = "-# Noch keine Daten vorhanden.";
        }

        return Container.of(
                TextDisplay.of("# \uD83D\uDCC8 Leaderboard"),
                TextDisplay.of("Hier siehst du die Top 10 Mitglieder des Discords!"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of(userLevels)
        );
    }
}
