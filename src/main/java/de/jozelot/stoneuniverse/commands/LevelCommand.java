package de.jozelot.stoneuniverse.commands;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.interfaces.Command;
import de.jozelot.stoneuniverse.mechanics.levelSystem.LevelUI;
import de.jozelot.stoneuniverse.mechanics.levelSystem.UserLevel;
import de.jozelot.stoneuniverse.util.Messages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelCommand implements Command {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(LevelCommand.class);

    public LevelCommand(StoneUniverse bot) {
        this.bot = bot;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("level", "🏆 | Admin: Verwalte die Level und XP der Mitglieder.")
                .addSubcommandGroups(new SubcommandGroupData("get", "🏆 | Admin: Schau dir die Daten eines Mitglieds an")
                        .addSubcommands(
                                new SubcommandData("xp", "🏆 | Admin: Zeigt die XP eines Mitglieds an")
                                        .addOption(OptionType.USER, "user", "Das Mitglied", true),
                                new SubcommandData("level", "🏆 | Admin: Zeigt das Level eines Mitglieds an")
                                        .addOption(OptionType.USER, "user", "Das Mitglied", true)
                        )
                )
                .addSubcommandGroups(new SubcommandGroupData("set", "🏆 | Admin: Setze die Daten eines Mitglieds fest.")
                        .addSubcommands(
                                new SubcommandData("xp", "🏆 | Admin: Setzt die XP eines Mitglieds auf einen festen Wert")
                                        .addOption(OptionType.USER, "user", "Das Mitglied", true)
                                        .addOption(OptionType.INTEGER, "anzahl", "Die genaue Anzahl an XP", true),
                                new SubcommandData("level", "🏆 | Admin: Setzt das Level eines Mitglieds auf einen festen Wert")
                                        .addOption(OptionType.USER, "user", "Das Mitglied", true)
                                        .addOption(OptionType.INTEGER, "anzahl", "Das genaue Level", true)
                        )
                )
                .addSubcommandGroups(new SubcommandGroupData("add", "🏆 | Admin: Füge einem Mitglied Daten hinzu.")
                        .addSubcommands(
                                new SubcommandData("xp", "🏆 | Admin: Gibt einem Mitglied zusätzliche XP")
                                        .addOption(OptionType.USER, "user", "Das Mitglied", true)
                                        .addOption(OptionType.INTEGER, "anzahl", "Wie viel XP hinzugefügt werden", true),
                                new SubcommandData("level", "🏆 | Admin: Gibt einem Mitglied zusätzliche Level")
                                        .addOption(OptionType.USER, "user", "Das Mitglied", true)
                                        .addOption(OptionType.INTEGER, "anzahl", "Wie viele Level hinzugefügt werden", true)
                        )
                )
                .addSubcommandGroups(new SubcommandGroupData("remove", "🏆 | Admin: Ziehe einem Mitglied Daten ab.")
                        .addSubcommands(
                                new SubcommandData("xp", "🏆 | Admin: Zieht einem Mitglied XP ab")
                                        .addOption(OptionType.USER, "user", "Das Mitglied", true)
                                        .addOption(OptionType.INTEGER, "anzahl", "Wie viel XP abgezogen werden", true),
                                new SubcommandData("level", "🏆 | Admin:  Zieht einem Mitglied Level ab")
                                        .addOption(OptionType.USER, "user", "Das Mitglied", true)
                                        .addOption(OptionType.INTEGER, "anzahl", "Wie viele Level abgezogen werden", true)
                        )
                );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        logger.info(event.getMember().getEffectiveName() + " issued server command: /" + event.getFullCommandName());
        String group = event.getSubcommandGroup();
        String subcommand = event.getSubcommandName();

        if (group == null || subcommand == null) {
            event.replyComponents(Messages.getError("Invalid Command")).setEphemeral(true).queue();
            return;
        }

        var targetUser = event.getOption("user").getAsUser();
        var anzahlOption = event.getOption("anzahl");
        int anzahl = (anzahlOption != null) ? anzahlOption.getAsInt() : 0;

        var levelSystem = bot.getBootstrap().getLevelSystem();
        LevelUI levelUI = levelSystem.getLevelUI();
        UserLevel userLevel = levelSystem.getUserLevel(targetUser.getIdLong());

        switch (group.toLowerCase()) {
            case "get" -> {
                if (subcommand.equalsIgnoreCase("xp")) {
                    event.replyComponents(levelUI.getXpDisplay(targetUser, userLevel))
                            .useComponentsV2().setEphemeral(true).queue();
                } else if (subcommand.equalsIgnoreCase("level")) {
                    int rank = levelSystem.getRank(userLevel);
                    event.replyComponents(levelUI.getLevelDisplay(targetUser, userLevel, rank))
                            .useComponentsV2().setEphemeral(true).queue();
                }
            }
            case "set" -> {
                if (anzahl < 0) {
                    event.replyComponents(Messages.getError("Amount can't be negative")).setEphemeral(true).queue();
                    return;
                }
                if (subcommand.equalsIgnoreCase("xp")) {
                    userLevel.setXp(anzahl);
                    levelSystem.saveUserLevel(userLevel);
                    event.replyComponents(levelUI.getXpChangedSuccess(targetUser, "set", anzahl, userLevel.getLevel()))
                            .useComponentsV2().setEphemeral(true).queue();
                } else if (subcommand.equalsIgnoreCase("level")) {
                    userLevel.setLevelDirect(anzahl);
                    levelSystem.saveUserLevel(userLevel);
                    event.replyComponents(levelUI.getLevelChangedSuccess(targetUser, "set", anzahl, userLevel.getLevel()))
                            .useComponentsV2().setEphemeral(true).queue();
                }
            }
            case "add" -> {
                if (anzahl <= 0) {
                    event.replyComponents(Messages.getError("Amount has to be 1 or more")).setEphemeral(true).queue();
                    return;
                }
                if (subcommand.equalsIgnoreCase("xp")) {
                    userLevel.addXp(anzahl);
                    levelSystem.saveUserLevel(userLevel);
                    event.replyComponents(levelUI.getXpChangedSuccess(targetUser, "add", anzahl, userLevel.getLevel()))
                            .useComponentsV2().setEphemeral(true).queue();
                } else if (subcommand.equalsIgnoreCase("level")) {
                    userLevel.addLevelAndCheck(anzahl);
                    levelSystem.saveUserLevel(userLevel);
                    event.replyComponents(levelUI.getLevelChangedSuccess(targetUser, "add", anzahl, userLevel.getLevel()))
                            .useComponentsV2().setEphemeral(true).queue();
                }
            }
            case "remove" -> {
                if (anzahl <= 0) {
                    event.replyComponents(Messages.getError("Amount has to be 1 or more")).setEphemeral(true).queue();
                    return;
                }
                if (subcommand.equalsIgnoreCase("xp")) {
                    userLevel.removeXp(anzahl);
                    levelSystem.saveUserLevel(userLevel);
                    event.replyComponents(levelUI.getXpChangedSuccess(targetUser, "remove", anzahl, userLevel.getLevel()))
                            .useComponentsV2().setEphemeral(true).queue();
                } else if (subcommand.equalsIgnoreCase("level")) {
                    userLevel.removeLevelAndCheck(anzahl);
                    levelSystem.saveUserLevel(userLevel);
                    event.replyComponents(levelUI.getLevelChangedSuccess(targetUser, "remove", anzahl, userLevel.getLevel()))
                            .useComponentsV2().setEphemeral(true).queue();
                }
            }
        }
    }

    @Override
    public String getGuildId() {
        long id = bot.getBootstrap().getConfig().getBotConf().getGuildId();
        return String.valueOf(id);
    }

    @Override
    public Permission getPermission() {
        return Permission.ADMINISTRATOR;
    }
}
