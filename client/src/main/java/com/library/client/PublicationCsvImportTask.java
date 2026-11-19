
package com.library.client;

import com.library.client.http.PublicationHttpClient;
import com.library.client.model.Publication;

import javafx.concurrent.Task;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PublicationCsvImportTask extends Task<Integer> {

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

        List<String> lines = Files.readAllLines(
                file.toPath(),
                StandardCharsets.UTF_8
        );

        if (lines.size() <= 1) {
            throw new IllegalArgumentException(
                    "CSV-файл не містить даних для імпорту."
            );
        }

        int total = lines.size() - 1;
        int imported = 0;

        for (int i = 1; i < lines.size(); i++) {

            if (isCancelled()) {
                break;
            }

            String line = lines.get(i);

            if (line.trim().isEmpty()) {
                continue;
            }

            List<String> values = parseCsvLine(line);

            if (values.size() != 5) {
                throw new IllegalArgumentException(
                        "Помилка у рядку " + (i + 1) +
                                ": потрібно 5 стовпців."
                );
            }

            String title = values.get(0).trim();
            String author = values.get(1).trim();

            if (title.isEmpty() || author.isEmpty()) {
                throw new IllegalArgumentException(
                        "Порожня назва або автор у рядку " + (i + 1)
                );
            }

            int year = Integer.parseInt(values.get(2).trim());
            int totalCopies = Integer.parseInt(values.get(3).trim());
            int availableCopies = Integer.parseInt(values.get(4).trim());

            if (year <= 0 || totalCopies < 0 ||
                    availableCopies < 0 ||
                    availableCopies > totalCopies) {

                throw new IllegalArgumentException(
                        "Некоректні дані у рядку " + (i + 1)
                );
            }

            Publication publication = new Publication(
                    0,
                    title,
                    author,
                    year,
                    totalCopies,
                    availableCopies
            );

            publicationClient.addPublication(publication);

            imported++;

            updateProgress(i, total);
            updateMessage(
                    "Імпортовано " + imported + " із " + total
            );
        }

        return imported;
    }

    private List<String> parseCsvLine(String line) {

        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (c == '"') {

                if (insideQuotes &&
                        i + 1 < line.length() &&
                        line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {
                    insideQuotes = !insideQuotes;
                }

            } else if (c == ',' && !insideQuotes) {

                values.add(current.toString());
                current.setLength(0);

            } else {
                current.append(c);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "Незакриті лапки у CSV-рядку."
            );
        }

        values.add(current.toString());

        return values;
    }
}
