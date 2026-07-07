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
import net.dv8tion.jda.api.utils.Timestamp;

import javax.security.auth.login.CredentialException;


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

        return Container.of(
                TextDisplay.of("# \uD83C\uDF89 " + giveaway.getTitel()),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of(giveaway.getDescription()),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**Endet:** <t:" + discordTimestamp + ":R> (<t:" + discordTimestamp + ":F>)\n" +
                        "**Gewinner:** " + giveaway.getWinnerCount() + "\n" +
                        "**Host:** " + "<@" + giveaway.getCreatorId() + ">\n" +
                        "**Teilnehmer:** " + giveaway.getEntryCount()),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("-# ID: `" + giveaway.getId() + "`"),

                ActionRow.of(
                        Button.of(ButtonStyle.PRIMARY, "giveaway:enter:" + giveaway.getId(), "Teilnehmen • " + giveaway.getEntryCount(), Emoji.fromUnicode("\uD83C\uDF89"))
                )
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
}
