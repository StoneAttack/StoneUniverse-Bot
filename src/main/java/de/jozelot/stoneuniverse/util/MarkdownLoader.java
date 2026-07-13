package de.jozelot.stoneuniverse.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MarkdownLoader {

    public static String loadMarkdown(String fileName) throws IOException {
        Path path = Paths.get("markdowns", fileName);

        if (!Files.exists(path)) {
            throw new java.io.FileNotFoundException("Could not find " + path.toAbsolutePath() + ".");
        }

        return Files.readString(path);
    }
}