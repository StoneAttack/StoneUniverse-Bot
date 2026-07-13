package de.jozelot.stoneuniverse.messages;

import de.jozelot.stoneuniverse.StoneUniverse;
import de.jozelot.stoneuniverse.util.MarkdownLoader;
import de.jozelot.stoneuniverse.util.MarkdownParser;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Regelwerk {

    private final StoneUniverse bot;
    private static final Logger logger = LoggerFactory.getLogger(Regelwerk.class);

    String discordRegeln;
    String stoneRegeln;

    public Regelwerk(StoneUniverse bot) {
        this.bot = bot;
    }

    public void sendRegelwerk(TextChannel channel) {
        Container container = Container.of(
                MediaGallery.of(MediaGalleryItem.fromUrl("https://cdn.discordapp.com/attachments/1416480939286069409/1520171878759661578/6f97905c-9075-4495-9715-9769e7bcf87c.png?ex=6a55faa3&is=6a54a923&hm=b7b83e60aad47855debb3b7202a3746c5ef358282f4f0dafebdaa647363f974e&")),
                TextDisplay.of("# Regelwerk"),
                TextDisplay.of("Wähle **Discord** oder **StoneAttack** - die Regeln öffnen sich privat für dich."),

                Separator.createDivider(Separator.Spacing.SMALL),

                ActionRow.of(
                        Button.of(ButtonStyle.SECONDARY, "regeln:discord", "Discord", Emoji.fromCustom("discord", 1478842608179744770L, false)),
                        Button.of(ButtonStyle.SECONDARY, "regeln:stoneattack", "Stoneattack", Emoji.fromCustom("stoneattack", 1478843084887298261L, false))
                ),

                Separator.createDivider(Separator.Spacing.SMALL),

                TextDisplay.of("-# 08.07.2026  •  07:55 Uhr")
        );
        channel.sendMessageComponents(container).useComponentsV2().queue();
    }

    public boolean initialize() {
        return initializeStoneRegeln() && initializeDiscordRegeln();
    }

    private boolean initializeDiscordRegeln() {
        copyFile("discord_regeln.md");
        try {
            discordRegeln = MarkdownLoader.loadMarkdown("discord_regeln.md");
            logger.info("Discord Rules loaded!");
            return true;
        } catch (IOException e) {
            logger.error("Discord Rules could not be read from disk!", e);
            return false;
        }
    }

    private boolean initializeStoneRegeln() {
        copyFile("stoneattack_regeln.md");
        try {
            stoneRegeln = MarkdownLoader.loadMarkdown("stoneattack_regeln.md");
            logger.info("Stoneattack Rules loaded!");
            return true;
        } catch (IOException e) {
            logger.error("Stoneattack Rules could not be read from disk!", e);
            return false;
        }
    }

    private void copyFile(String fileName) {
        File directory = new File("markdowns");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File configFile = new File(directory, fileName);

        if (!configFile.exists()) {
            logger.info(fileName + " not found. Creating default configuration file.");
            String resourcePath = "markdowns/" + fileName;
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (in == null) {
                    logger.error("Default " + resourcePath + " could not be found inside the jar resources!");
                    return;
                }
                Files.copy(in, configFile.toPath());
            } catch (Exception e) {
                logger.error("Failed to copy default file: " + fileName, e);
            }
        }
    }

    public void updateDiscordRegeln(String newContent) throws IOException {
        this.discordRegeln = newContent;
        File file = new File("markdowns", "discord_regeln.md");
        Files.writeString(file.toPath(), newContent);
        logger.info("Discord Rules updated by an admin!");
    }

    public void updateStoneRegeln(String newContent) throws IOException {
        this.stoneRegeln = newContent;
        File file = new File("markdowns", "stoneattack_regeln.md");
        Files.writeString(file.toPath(), newContent);
        logger.info("Stoneattack Rules updated by an admin!");
    }

    public File getFile(String type) {
        return new File("markdowns", type.toLowerCase().equals("discord") ? "discord_regeln.md" : "stoneattack_regeln.md");
    }

    public Container getDiscordRegeln() {
        return MarkdownParser.parse(discordRegeln);
    }

    public Container getStoneRegeln() {
        return MarkdownParser.parse(stoneRegeln);
    }
}
