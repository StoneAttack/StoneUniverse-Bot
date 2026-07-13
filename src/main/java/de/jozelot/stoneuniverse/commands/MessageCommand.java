package de.jozelot.stoneuniverse.commands;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.interfaces.Command;
import de.jozelot.stoneuniverse.messages.Regelwerk;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class MessageCommand implements Command {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(MessageCommand.class);

    public MessageCommand(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("messages", "📬 | Admin: Sende und verwalte Nachrichten Presets.")
                .addSubcommands(
                        new SubcommandData("send", "📬 | Admin: Sende Nachrichten Presets.")
                                .addOption(OptionType.STRING, "preset", "Das Preset", true, true)
                                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Kanal zum Senden").setChannelTypes(ChannelType.TEXT)),

                        new SubcommandData("export", "📥 | Admin: Exportiert die aktuelle Regel-Datei zum Bearbeiten.")
                                .addOption(OptionType.STRING, "typ", "Welche Regeln?", true, true),

                        new SubcommandData("import", "📤 | Admin: Importiert eine bearbeitete Regel-Datei.")
                                .addOption(OptionType.STRING, "typ", "Welche Regeln?", true, true)
                                .addOption(OptionType.ATTACHMENT, "datei", "Die bearbeitete .md Datei", true)
                );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        Regelwerk regelwerk = bot.getBootstrap().getMessageManager().getRegelwerk();
        logger.info(event.getMember().getEffectiveName() + " issued server command: /" + event.getFullCommandName());

        if ("send".equalsIgnoreCase(subcommand)) {
            String option = event.getOption("preset").getAsString();
            OptionMapping channelOpt = event.getOption("channel");
            TextChannel channel = (channelOpt != null) ? channelOpt.getAsChannel().asTextChannel() : event.getChannel().asTextChannel();

            if (option.equalsIgnoreCase("regeln")) {
                bot.getBootstrap().getMessageManager().getRegelwerk().sendRegelwerk(channel);
                event.replyComponents(bot.getBootstrap().getMessageManager().getSendSuccess(option, channel)).useComponentsV2().setEphemeral(true).queue();
                return;
            }
            event.replyComponents(Messages.getError("Preset not found!")).useComponentsV2().setEphemeral(true).queue();
            return;
        }

        if ("export".equalsIgnoreCase(subcommand)) {
            String typ = event.getOption("typ").getAsString().toLowerCase();
            if (!typ.equals("discord") && !typ.equals("stoneattack")) {
                event.replyComponents(Messages.getError("Invalid type: Use 'discord' or 'stoneattack'.")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            File file = regelwerk.getFile(typ);
            if (!file.exists()) {
                event.replyComponents(Messages.getError("File does not exist on our servers!")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            event.replyComponents(Messages.getDownload(typ, file)).useComponentsV2().setEphemeral(true).queue();
            return;
        }

        if ("import".equalsIgnoreCase(subcommand)) {
            event.deferReply(true).queue();

            String typ = event.getOption("typ").getAsString().toLowerCase();
            Message.Attachment attachment = event.getOption("datei").getAsAttachment();

            if (!attachment.getFileName().endsWith(".md")) {
                event.getHook().sendMessageComponents(Messages.getError("Only `.md` (Markdown) Files are allowed!")).useComponentsV2().setEphemeral(true).queue();
                return;
            }

            attachment.getProxy().download().thenAccept(inputStream -> {
                try {
                    String neuerInhalt = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

                    if (typ.equals("discord")) {
                        regelwerk.updateDiscordRegeln(neuerInhalt);
                    } else if (typ.equals("stoneattack")) {
                        regelwerk.updateStoneRegeln(neuerInhalt);
                    } else {
                        event.getHook().sendMessageComponents(Messages.getError("Invalid type!")).useComponentsV2().setEphemeral(true).queue();
                        return;
                    }

                    event.getHook().sendMessageComponents(Messages.getUpload(typ)).useComponentsV2().setEphemeral(true).queue();

                } catch (Exception e) {
                    e.printStackTrace();
                    event.getHook().sendMessageComponents(Messages.getError("Error while working with your file")).useComponentsV2().setEphemeral(true).queue();
                }
            }).exceptionally(throwable -> {
                event.getHook().sendMessageComponents(Messages.getError("Error while downloading from discord")).useComponentsV2().setEphemeral(true).queue();
                return null;
            });
        }
    }

    @Override
    public void autoComplete(CommandAutoCompleteInteractionEvent event) {
        String focusedOption = event.getFocusedOption().getName();
        String currentInput = event.getFocusedOption().getValue().toLowerCase();

        if (focusedOption.equalsIgnoreCase("preset")) {
            List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = Stream.of(
                            new net.dv8tion.jda.api.interactions.commands.Command.Choice("Regelwerk", "regeln")
                    )
                    .filter(c -> c.getName().toLowerCase().contains(currentInput))
                    .toList();

            event.replyChoices(choices).queue();
            return;
        }

        if (focusedOption.equalsIgnoreCase("typ")) {
            List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = Stream.of(
                            new net.dv8tion.jda.api.interactions.commands.Command.Choice("Discord", "discord"),
                            new net.dv8tion.jda.api.interactions.commands.Command.Choice("StoneAttack", "stoneattack")
                    )
                    .filter(c -> c.getName().toLowerCase().contains(currentInput))
                    .toList();

            event.replyChoices(choices).queue();
        }
    }

    @Override
    public String getGuildId() {
        long id = bot.getBootstrap().getConfig().getBotConf().getGuildId();
        return String.valueOf(id);
    }

    @Override
    public Permission getPermission() {
        return Permission.MANAGE_SERVER;
    }
}