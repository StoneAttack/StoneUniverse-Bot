package de.jozelot.stoneuniverse.mechanics.tempChannels;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.data.config.ConfigManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.modals.Modal;

public class TempChannelUI {

    private final StoneUniverse bot;

    public TempChannelUI(StoneUniverse bot) {
        this.bot = bot;
    }

    public Container getSettingsMessage(long creatorId, VoiceChannel channel, boolean isLocked) {
        Role everyoneRole = channel.getGuild().getPublicRole();
        PermissionOverride override = channel.getPermissionOverride(everyoneRole);

        Button visibilityButton;
        if (isLocked) {
            visibilityButton = Button.success("tempchannel:unlock:" + channel.getId(), "Kanal entsperren")
                    .withEmoji(Emoji.fromUnicode("🔓"));
        } else {
            visibilityButton = Button.danger("tempchannel:lock:" + channel.getId(), "Kanal sperren")
                    .withEmoji(Emoji.fromUnicode("🔒"));
        }

        return Container.of(
                TextDisplay.of("## \uD83C\uDF99\uFE0F Temp Channel Einstellungen"),
                TextDisplay.of("-# <@" + creatorId + "> Hier kannst du Einstellungen für deinen Temp Channel festlegen."),

                Separator.createDivider(Separator.Spacing.LARGE),

                TextDisplay.of("### Kosmetische Einstellungen"),
                ActionRow.of(
                        Button.of(ButtonStyle.PRIMARY, "tempchannel:change_name:" + channel.getId(), "Namen ändern", Emoji.fromUnicode("✏\uFE0F")),
                        visibilityButton,
                        Button.of(ButtonStyle.SECONDARY, "tempchannel:change_limit:" + channel.getId(), "Nutzerlimit ändern", Emoji.fromUnicode("\uD83D\uDC65"))
                ),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("### Nutzer Einstellungen"),
                ActionRow.of(
                        Button.of(ButtonStyle.PRIMARY, "tempchannel:invite:" + channel.getId(), "Nutzer einladen", Emoji.fromUnicode("➕")),
                        Button.of(ButtonStyle.SECONDARY, "tempchannel:change_owner:" + channel.getId(), "Besitzer übertragen", Emoji.fromUnicode("\uD83D\uDC51")),
                        Button.of(ButtonStyle.DANGER, "tempchannel:kick:" + channel.getId(), "Nutzer kicken", Emoji.fromUnicode("\uD83D\uDC62")),
                        Button.of(ButtonStyle.DANGER, "tempchannel:ban:" + channel.getId(), "Nutzer ausschließen", Emoji.fromUnicode("⛔")),
                        Button.of(ButtonStyle.SECONDARY, "tempchannel:unban:" + channel.getId(), "Ausschluss aufheben", Emoji.fromUnicode("\uD83D\uDD13")).withDisabled(true)
                )
        );
    }

    public Container getNewOwner(long oldOwnerId, long newOwnerId) {
        return Container.of(
                TextDisplay.of("## \uD83D\uDC51 Besitzer Änderung"),
                TextDisplay.of("-# Der Besitzer dieses Temp Channels hat sich geändert."),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**Änderung:** <@" + oldOwnerId + "> **->** <@" + newOwnerId + ">")
        );
    }

    public Container getChangeSuccess(String change) {
        return Container.of(
                TextDisplay.of("## ✅ Änderung erfolgreich"),
                TextDisplay.of("-# Deine Änderung wurde erfolgreich angewendet!"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("### Änderung:"),
                TextDisplay.of(change)
        );
    }

    public Modal getChangeName(String channelId, String currentName) {
        TextInput name = TextInput.create("tempchannel:change_name:value", TextInputStyle.SHORT)
                .setValue(currentName)
                .setRequired(true)
                .setRequiredRange(1, 100)
                .build();

        return Modal.create("tempchannel:change_name:" + channelId, "Namen ändern").addComponents(Label.of("Name", name)).build();
    }

    public Modal getChangeLimit(String channelId, int currentLimit) {
        TextDisplay info = TextDisplay.of("### Nummer zwischen 0 und 100. Die 0 löst das Limit auf.");

        TextInput limit = TextInput.create("tempchannel:change_limit:value", TextInputStyle.SHORT)
                .setValue(String.valueOf(currentLimit))
                .setRequired(true)
                .setRequiredRange(1, 2)
                .build();

        return Modal.create("tempchannel:change_limit:" + channelId, "Nutzerlimit ändern").addComponents(info, Label.of("Limit", limit)).build();
    }

    public EntitySelectMenu getMemberSelectMenu(String aktion) {
        return EntitySelectMenu.create("tempchannel:" + aktion +":value", EntitySelectMenu.SelectTarget.USER)
                .setPlaceholder("Wähle ein Mitglied aus...")
                .setRequiredRange(1, 1)
                .build();
    }

    public Modal getKickModal(String channelId) {
        return Modal.create("tempchannel:kick:" + channelId, "Nutzer kicken").addComponents(Label.of("Nutzer", getMemberSelectMenu("kick"))).build();
    }

    public Modal getOwnerModal(String channelId) {
        return Modal.create("tempchannel:change_owner:" + channelId, "Besitzer übertragen").addComponents(Label.of("Nutzer", getMemberSelectMenu("change_owner"))).build();
    }

    public Modal getBanModal(String channelId) {
        return Modal.create("tempchannel:ban:" + channelId, "Nutzer ausschließen").addComponents(Label.of("Nutzer", getMemberSelectMenu("ban"))).build();
    }

    public Modal getUnbanModal(VoiceChannel channel) {
        return Modal.create("tempchannel:unban:" + channel.getId(), "Ausschluss aufheben").addComponents(Label.of("Nutzer", getUnbanSelectMenu(channel))).build();
    }

    public Modal getInviteModal(String channelId) {
        return Modal.create("tempchannel:invite:" + channelId, "Nutzer einladen").addComponents(Label.of("Nutzer", getMemberSelectMenu("invite"))).build();
    }

    public StringSelectMenu getUnbanSelectMenu(VoiceChannel channel) {
        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("tempchannel:unban:value")
                .setPlaceholder("Wähle einen Nutzer aus, um die Sperre aufzuheben...")
                .setRequiredRange(1, 1);

        int count = 0;

        for (PermissionOverride override : channel.getPermissionOverrides()) {
            if (override.isMemberOverride() && override.getDenied().contains(Permission.VOICE_CONNECT)) {
                Member bannedMember = channel.getGuild().getMemberById(override.getIdLong());

                String displayName = (bannedMember != null) ? bannedMember.getEffectiveName() : "Unbekannter User (" + override.getId() + ")";

                menuBuilder.addOption(displayName, override.getId());
                count++;
            }
        }

        if (count == 0) {
            menuBuilder.addOption("Keine gesperrten Nutzer gefunden", "none");
        }

        return menuBuilder.build();
    }

    public Container getKickSuccess(long userId) {
        return Container.of(
                TextDisplay.of("## ✅ Nutzer gekickt"),
                TextDisplay.of("Du hast <@" + userId + "> erfolgreich aus dem Kanal entfernt!")
        );
    }

    public Container getBanSuccess(long userId) {
        return Container.of(
                TextDisplay.of("## ✅ Nutzer ausgeschlossen"),
                TextDisplay.of("Du hast <@" + userId + "> erfolgreich aus dem Kanal ausgeschlossen!")
        );
    }

    public Container getInviteSuccess(long userId) {
        return Container.of(
                TextDisplay.of("## ✅ Nutzer eingeladen"),
                TextDisplay.of("Du hast <@" + userId + "> erfolgreich in den Kanal eingeladen!")
        );
    }

    public Container getUnbanSuccess(long userId) {
        return Container.of(
                TextDisplay.of("## ✅ Ausschluss entfernt"),
                TextDisplay.of("Du hast den Ausschluss von <@" + userId + "> erfolgreich entfernt!")
        );
    }

    public Container getInvite(ConfigManager config, TempChannel tempChannel, long userId) {
        return Container.of(
                TextDisplay.of("# ➕ Temp Channel Einladung"),
                TextDisplay.of("-# Du wurdest auf **StoneUniverse** in einen Temp Channel eingeladen!"),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("**Kanal:** <#" + tempChannel.getChannelId() + ">\n" +
                        "**Besitzer:** <@" + tempChannel.getOwnerId() + ">\n" +
                        "**Eingeladen:** <@" + userId + ">")
        );
    }
}
