package com.library.client;

import com.library.client.http.PublicationHttpClient;
import com.library.client.model.Publication;

import javafx.concurrent.Task;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class PublicationCsvImportTask extends Task<Integer> {

    private static final Logger LOGGER =
            Logger.getLogger(PublicationCsvImportTask.class.getName());

    private static final Set<String> VALID_COMMA_HEADERS = Set.of(
            "title,author,year,totalCopies,availableCopies",
            "title,author,yearPublished,totalCopies,availableCopies"
    );

    private static final Set<String> VALID_SEMICOLON_HEADERS = Set.of(
            "title;author;year;totalCopies;availableCopies",
            "title;author;yearPublished;totalCopies;availableCopies"
    );

    private final File file;
    private final PublicationHttpClient publicationClient;

    public PublicationCsvImportTask(
            File file,
            PublicationHttpClient publicationClient) {

        this.file = file;
        this.publicationClient = publicationClient;
    }

    @Override
    protected Integer call() throws Exception {

        LOGGER.info("CSV import started: " + (file != null ? file.getName() : "null"));

        if (file == null || !file.exists() || !file.isFile()
                || !file.canRead()) {
            LOGGER.severe("CSV file not found or inaccessible");
            throw new IllegalArgumentException(
                    "CSV file not found or inaccessible."
            );
        }

        List<String> lines = Files.readAllLines(
                file.toPath(), StandardCharsets.UTF_8
        );

        if (lines.isEmpty()) {
            LOGGER.severe("CSV file is empty");
            throw new IllegalArgumentException("CSV file is empty.");
        }

        String header = lines.get(0).replace("\uFEFF", "").trim();
        char delimiter;

        if (VALID_COMMA_HEADERS.contains(header)) {
            delimiter = ',';
        } else if (VALID_SEMICOLON_HEADERS.contains(header)) {
            delimiter = ';';
        } else {
            LOGGER.severe("Invalid CSV header: " + header);
            throw new IllegalArgumentException(
                    "Invalid CSV header: " + header
            );
        }

        if (lines.size() <= 1) {
            LOGGER.severe("CSV file contains no data");
            throw new IllegalArgumentException(
                    "CSV file contains no data."
            );
        }

        int total = lines.size() - 1;
        int imported = 0;
        int skipped = 0;

        for (int i = 1; i < lines.size(); i++) {

            if (isCancelled()) {
                LOGGER.warning("CSV import cancelled");
                break;
            }

            String line = lines.get(i).replace("\uFEFF", "");

            if (line.trim().isEmpty()) {
                skipped++;
                LOGGER.warning("Skipped row " + (i + 1) + ": Empty line");
                updateProgress(i, total);
                updateMessage("Додано: " + imported + ", пропущено: " + skipped);
                continue;
            }

            try {
                List<String> values = parseCsvLine(line, delimiter);

                if (values.size() != 5) {
                    throw new IllegalArgumentException(
                            "Exactly 5 columns are required."
                    );
                }

                String title = values.get(0).trim();
                String author = values.get(1).trim();

                if (title.isEmpty() || author.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Title or author is empty."
                    );
                }

                int year = Integer.parseInt(values.get(2).trim());
                int totalCopies = Integer.parseInt(values.get(3).trim());
                int availableCopies = Integer.parseInt(values.get(4).trim());

                if (year <= 0 || totalCopies < 0
                        || availableCopies < 0
                        || availableCopies > totalCopies) {
                    throw new IllegalArgumentException(
                            "Invalid numeric values."
                    );
                }

                Publication publication = new Publication(
                        0, title, author, year,
                        totalCopies, availableCopies
                );

                publicationClient.addPublication(publication);

                imported++;
                LOGGER.info("Imported row " + (i + 1) + ": " + title);

            } catch (Exception e) {

                skipped++;
                LOGGER.warning(
                        "Skipped row " + (i + 1) +
                                ": " + e.getMessage()
                );
            }

            updateProgress(i, total);
            updateMessage(
                    "Додано: " + imported +
                            ", пропущено: " + skipped
            );
        }

        LOGGER.info(
                "CSV import completed. Imported: " + imported +
                        ", skipped: " + skipped
        );

        return imported;
    }

    private List<String> parseCsvLine(String line, char delimiter) {

        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (c == '"') {
                if (insideQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }

            } else if (c == delimiter && !insideQuotes) {
                values.add(current.toString());
                current.setLength(0);

            } else {
                current.append(c);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "Unclosed quotation marks."
            );
        }

        values.add(current.toString());
        return values;
    }
}