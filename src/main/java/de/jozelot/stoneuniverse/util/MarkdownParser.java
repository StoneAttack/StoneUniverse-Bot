package de.jozelot.stoneuniverse.util;

import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import java.util.ArrayList;
import java.util.List;

public class MarkdownParser {

    public static Container parse(String markdown) {
        List<ContainerChildComponent> components = new ArrayList<>();
        String[] lines = markdown.split("\r?\n");
        StringBuilder currentTextBlock = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.equals("----")) {
                flushTextBlock(components, currentTextBlock);
                components.add(Separator.createDivider(Separator.Spacing.LARGE));
                continue;
            }

            if (trimmed.equals("---")) {
                flushTextBlock(components, currentTextBlock);
                components.add(Separator.createDivider(Separator.Spacing.SMALL));
                continue;
            }

            if (trimmed.isEmpty()) {
                flushTextBlock(components, currentTextBlock);
                continue;
            }

            if (currentTextBlock.length() > 0) {
                currentTextBlock.append("\n");
            }
            currentTextBlock.append(line);
        }

        flushTextBlock(components, currentTextBlock);

        return Container.of(components);
    }

    private static void flushTextBlock(List<ContainerChildComponent> components, StringBuilder currentTextBlock) {
        if (currentTextBlock.length() > 0) {
            components.add(TextDisplay.of(currentTextBlock.toString()));
            currentTextBlock.setLength(0);
        }
    }
}