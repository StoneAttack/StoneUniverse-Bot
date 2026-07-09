package de.jozelot.stoneuniverse.mechanics.giveaway;

import de.jozelot.stoneuniverse.StoneUniverse;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.modals.Modal;

import java.awt.*;
import java.util.List;


public class GiveawayUI {

    private final StoneUniverse bot;

    public GiveawayUI(StoneUniverse bot) {
        this.bot = bot;
    }

    public Modal getSetupModal(String channelId) {
        TextInput titleInput = TextInput.create("giveaway:titel", TextInputStyle.SHORT)
                .setPlaceholder("[PFLICHT]")
                .setRequired(true)
                .setMaxLength(60)
                .build();

        TextInput descriptionInput = TextInput.create("giveaway:description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("[OPTIONAL]")
                .setRequired(false)
                .setMaxLength(2000)
                .build();

        TextInput entryLimit = TextInput.create("giveaway:entry_limit", TextInputStyle.SHORT)
                .setPlaceholder("Default: Unlimited")
                .setRequired(false)
                .setMinLength(1)
                .setMaxLength(7)
                .build();

        TextInput winnerCount = TextInput.create("giveaway:winner", TextInputStyle.SHORT)
                .setPlaceholder("Default: 1")
                .setRequired(false)
                .setMinLength(1)
                .setMaxLength(7)
                .build();

        TextInput rollDate = TextInput.create("giveaway:roll_date", TextInputStyle.SHORT)
                .setPlaceholder("1d 1h 1m - Kann Kombiniert werden")
                .setRequired(true)
                .setMaxLength(20)
                .build();

        return Modal.create("giveaway:setup:" + channelId, "Giveaway erstellen")
                .addComponents(
                        /*TextDisplay.of("# Willkommen zum Giveaway Setup!\n" +
                                "-# Hier kannst du alles einstellen was du für ein Giveaway brauchst."),*/

                        Label.of("Titel", titleInput),
                        Label.of("Beschreibung", descriptionInput),
                        Label.of("Teilnehmer Limit", entryLimit),
                        Label.of("Gewinner Zahl", winnerCount),
                        Label.of("Auslose Zeitpunkt", rollDate)
                        //Label.of("test", Checkbox.of("giveawasfasdasdsd", false))
                )
                .build();
    }

    public Container getSetupSuccess() {
        return Container.of(
                TextDisplay.of("# ✅ Giveaway erstellt"),
                TextDisplay.of("Dein Giveaway wurde erfolgreich erstellt!")
        );
    }

    public Container getGiveawayMessage(Giveaway giveaway) {
        long drawDateMs = giveaway.getDrawDate();
        long discordTimestamp = drawDateMs / 1000;
        String teilnahmeLimit = giveaway.getEntryLimit() == 0 ? "" : "\n**Teilnahme-Limit:** " + giveaway.getEntryLimit();

        String description = giveaway.getDescription().equalsIgnoreCase("") ? "Keine Beschreibung angegeben." : giveaway.getDescription();

        return Container.of(
                TextDisplay.of("# \uD83C\uDF89 " + giveaway.getTitel()),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of(description),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**Endet:** <t:" + discordTimestamp + ":R> (<t:" + discordTimestamp + ":F>)\n" +
                        "**Gewinner:** " + giveaway.getWinnerCount() + "\n" +
                        "**Host:** " + "<@" + giveaway.getCreatorId() + ">\n" +
                        "**Teilnehmer:** " + giveaway.getEntryCount() + teilnahmeLimit
                ),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("-# ID: `" + giveaway.getId() + "`"),

                ActionRow.of(
                        Button.of(ButtonStyle.PRIMARY, "giveaway:enter:" + giveaway.getId(), "Teilnehmen • " + giveaway.getEntryCount(), Emoji.fromUnicode("\uD83C\uDF89"))
                )
        ).withAccentColor(new Color(0x5865F2));
    }

    public Container getGiveawayMessageRolled(Giveaway giveaway) {
        long drawDateMs = System.currentTimeMillis();
        long discordTimestamp = drawDateMs / 1000;
        String teilnahmeLimit = giveaway.getEntryLimit() == 0 ? "" : "\n**Teilnahme-Limit:** " + giveaway.getEntryLimit();

        String winnerText;
        if (giveaway.getWinner().isEmpty()) {
            winnerText = "Es gab keine Teilnehmer, somit konnte kein Gewinner gezogen werden.";
        } else {
            StringBuilder sb = new StringBuilder();
            giveaway.getWinner().forEach(winnerId -> sb.append("• <@").append(winnerId).append(">\n"));
            winnerText = sb.toString();
        }

        String description = giveaway.getDescription().equalsIgnoreCase("") ? "Keine Beschreibung angegeben." : giveaway.getDescription();

        return Container.of(
                TextDisplay.of("# \uD83C\uDF89 " + giveaway.getTitel()),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of(description),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**Beendet:** <t:" + discordTimestamp + ":R> (<t:" + discordTimestamp + ":F>)\n" +
                        "**Gewinner:** " + giveaway.getWinnerCount() + "\n" +
                        "**Host:** <@" + giveaway.getCreatorId() + ">\n" +
                        "**Teilnehmer:** " + giveaway.getEntryCount() + teilnahmeLimit
                ),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("## \uD83C\uDFC6 Gewinner"),
                TextDisplay.of(winnerText),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("-# ID: `" + giveaway.getId() + "`"),

                ActionRow.of(
                        Button.of(ButtonStyle.SECONDARY, "giveaway:ended:" + giveaway.getId(), "Beendet", Emoji.fromUnicode("\uD83C\uDF89")).withDisabled(true)
                )
        ).withAccentColor(new Color(0x747F8D));
    }

    public Container getGiveawayRollSuccess(Giveaway giveaway) {
        String winnerText;
        if (giveaway.getWinner().isEmpty()) {
            winnerText = "Es gab keine Teilnehmer, somit konnte kein Gewinner gezogen werden.";
        } else {
            StringBuilder sb = new StringBuilder();
            giveaway.getWinner().forEach(winnerId -> sb.append("• <@").append(winnerId).append(">\n"));
            winnerText = sb.toString();
        }

        return Container.of(
                TextDisplay.of("# \uD83C\uDF89 Giveaway Beendet"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**Giveaway:** " + giveaway.getTitel()),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("## \uD83C\uDFC6 Gewinner"),
                TextDisplay.of(winnerText),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("-# ID: `" + giveaway.getId() + "`")
        );
    }

    public Container getGiveawayPreRollReply(Giveaway giveaway) {
        return Container.of(
                TextDisplay.of("# ✅ Giveaway Vorzeitig Beendet"),
                TextDisplay.of("Du hast das Giveaway `" + giveaway.getTitel() + "` erfolgreich beendet!")
        );
    }

    public Container getGiveawayEnterError(GiveawayEnterError giveawayEnterError, Giveaway giveaway) {
        String error = giveawayEnterError.getText();

        if (giveawayEnterError == GiveawayEnterError.ALREAD_IN) {
            return Container.of(
                    TextDisplay.of("# \uD83D\uDCDB Teilnahme Fehler"),
                    TextDisplay.of(error),
                    ActionRow.of(Button.of(ButtonStyle.DANGER, "giveaway:leave:" + giveaway.getId(), "Verlassen", Emoji.fromUnicode("⛔"))
            ));
        }
        return Container.of(
                TextDisplay.of("# \uD83D\uDCDB Teilnahme Fehler"),
                TextDisplay.of(error)
        );
    }

    public Container getGiveawayEnterSuccess(Giveaway giveaway) {
        return Container.of(
                TextDisplay.of("# ✅ Giveaway Teilnahme Erfolgreich"),
                TextDisplay.of("Du bist dem Giveaway `" + giveaway.getTitel() + "` erfolgreich beigetreten. Viel Glück!")
        );
    }

    public Container getGiveawayLeaveSuccess(Giveaway giveaway) {
        return Container.of(
                TextDisplay.of("# ✅ Giveaway Austritt Erfolgreich"),
                TextDisplay.of("Du bist dem Giveaway `" + giveaway.getTitel() + "` erfolgreich ausgetreten.")
        );
    }

    public Container getGiveaways(String guildId) {
        var giveawayService = bot.getBootstrap().getGiveawayService();
        List<Giveaway> giveaways = giveawayService.getGiveaways();

        StringBuilder sb = new StringBuilder();

        if (giveaways.isEmpty()) {
            sb.append("Es sind keine aktiven Giveaways vorhanden.");
        } else {
            giveaways.forEach(giveaway -> {
                String messageJumpUrl = String.format("https://discord.com/channels/%s/%d/%d",
                        guildId,
                        giveaway.getChannelId(),
                        giveaway.getMessageId()
                );

                sb.append("• **").append(giveaway.getTitel()).append("** | Einträge: `")
                        .append(giveaway.getEntryCount()).append("` | ID: `").append(giveaway.getId())
                        .append("` — [[Nachricht]](").append(messageJumpUrl).append(")\n");
            });
        }
        return Container.of(
                TextDisplay.of("# 📋 Aktive Giveaways"),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.of(sb.toString())
        );
    }

    public Container getGiveawayCancelSuccess(Giveaway giveaway) {
        return Container.of(
                TextDisplay.of("# \uD83D\uDCDB Giveaway Abgebrochen"),
                TextDisplay.of("-# Das Giveaway wurde abgebrochen!"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**Giveaway:** " + giveaway.getTitel()),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("-# ID: `" + giveaway.getId() + "`")
        );
    }

    public Container getGiveawayMessageCancel(Giveaway giveaway) {
        long drawDateMs = System.currentTimeMillis();
        long discordTimestamp = drawDateMs / 1000;
        String teilnahmeLimit = giveaway.getEntryLimit() == 0 ? "" : "\n**Teilnahme-Limit:** " + giveaway.getEntryLimit();

        String description = giveaway.getDescription().equalsIgnoreCase("") ? "Keine Beschreibung angegeben." : giveaway.getDescription();

        return Container.of(
                TextDisplay.of("# \uD83C\uDF89 " + giveaway.getTitel()),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of(description),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**Beendet:** <t:" + discordTimestamp + ":R> (<t:" + discordTimestamp + ":F>)\n" +
                        "**Gewinner:** " + giveaway.getWinnerCount() + "\n" +
                        "**Host:** <@" + giveaway.getCreatorId() + ">\n" +
                        "**Teilnehmer:** " + giveaway.getEntryCount() + teilnahmeLimit
                ),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("## Abgebrochen"),
                TextDisplay.of("-# Das Giveaway wurde abgebrochen!"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("-# ID: `" + giveaway.getId() + "`"),

                ActionRow.of(
                        Button.of(ButtonStyle.SECONDARY, "giveaway:ended:" + giveaway.getId(), "Beendet", Emoji.fromUnicode("\uD83C\uDF89")).withDisabled(true)
                )
        ).withAccentColor(new Color(0x747F8D));
    }

    public Container getGiveawayCancelReply(Giveaway giveaway) {
        return Container.of(
                TextDisplay.of("# ✅ Giveaway Abgebrochen"),
                TextDisplay.of("Du hast das Giveaway `" + giveaway.getTitel() + "` erfolgreich beendet!")
        );
    }
}
