
package com.library.client;

import com.library.client.http.*;
import com.library.client.model.*;
import java.util.List;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.time.LocalDate;
import java.util.function.Function;

public class Main extends Application {

    private final PublicationHttpClient publicationClient =
            new PublicationHttpClient();

    private final ReaderHttpClient readerClient =
            new ReaderHttpClient();

    private final BookingHttpClient bookingClient =
            new BookingHttpClient();

    private final IssuanceHttpClient issuanceClient =
            new IssuanceHttpClient();

    private final ObservableList<Publication> publications =
            FXCollections.observableArrayList();

    private final ObservableList<Reader> readers =
            FXCollections.observableArrayList();

    private final ObservableList<Booking> bookings =
            FXCollections.observableArrayList();

    private final ObservableList<Issuance> issuances =
            FXCollections.observableArrayList();

    private final TableView<Publication> publicationTable =
            new TableView<>();

    private final TableView<Reader> readerTable =
            new TableView<>();

    private final TableView<Booking> bookingTable =
            new TableView<>();

    private final TableView<Issuance> issuanceTable =
            new TableView<>();

    @Override
    public void start(Stage stage) {

        TabPane tabPane = new TabPane();

        tabPane.getTabs().addAll(
                createPublicationTab(),
                createReaderTab(),
                createBookingTab(),
                createIssuanceTab()
        );

        VBox root = new VBox(
                10,
                new Label("Бібліотечна система"),
                tabPane
        );

        root.setStyle("-fx-padding: 15;");

        Scene scene = new Scene(root, 1000, 600);

        stage.setTitle("Library System");
        stage.setScene(scene);
        stage.show();

        loadPublications();
        loadReaders();
        loadBookings();
        loadIssuances();
    }

    // ========== PUBLICATIONS ==========

    private Tab createPublicationTab() {

        addNumberColumn(publicationTable, "ID",
                Publication::getId);

        addStringColumn(publicationTable, "Назва",
                Publication::getTitle);

        addStringColumn(publicationTable, "Автор",
                Publication::getAuthor);

        addNumberColumn(publicationTable, "Рік",
                Publication::getYearPublished);

        addNumberColumn(publicationTable, "Усього",
                Publication::getTotalCopies);

        addNumberColumn(publicationTable, "Доступно",
                Publication::getAvailableCopies);

        publicationTable.setItems(publications);

        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> loadPublications());

        Button add = new Button("Додати");
        add.setOnAction(e -> addPublication());

        Button edit = new Button("Редагувати");
        edit.setOnAction(e -> editPublication());

        Button delete = new Button("Видалити");
        delete.setOnAction(e -> deletePublication());

        Button importCsv = new Button("Імпорт CSV");
        importCsv.setOnAction(e -> importPublicationsCsv());

        HBox buttons = new HBox(
                10, refresh, add, edit, delete, importCsv
        );

        return new Tab("Видання",
                new VBox(10, publicationTable, buttons));
    }

    private void loadPublications() {
        try {
            publications.setAll(
                    publicationClient.getAllPublications()
            );
        } catch (Exception e) {
            showError("Не вдалося завантажити видання: "
                    + e.getMessage());
        }
    }

    private void addPublication() {

        Dialog<Publication> dialog =
                createPublicationDialog(null);

        dialog.showAndWait().ifPresent(publication -> {
            try {
                publicationClient.addPublication(publication);
                loadPublications();
            } catch (Exception e) {
                showError(e.getMessage());
            }
        });
    }

    private void editPublication() {

        Publication selected =
                publicationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Спочатку виберіть видання.");
            return;
        }

        Dialog<Publication> dialog =
                createPublicationDialog(selected);

        dialog.showAndWait().ifPresent(publication -> {
            try {
                publication.setId(selected.getId());
                publicationClient.updatePublication(publication);
                loadPublications();
            } catch (Exception e) {
                showError(e.getMessage());
            }
        });
    }

    private void deletePublication() {

        Publication selected =
                publicationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Спочатку виберіть видання.");
            return;
        }

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Видалення");
        confirmation.setHeaderText("Видалити видання?");
        confirmation.setContentText(selected.getTitle());

        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    publicationClient.deletePublication(
                            selected.getId()
                    );
                    loadPublications();
                } catch (Exception e) {
                    showError(e.getMessage());
                }
            }
        });
    }

    private Dialog<Publication> createPublicationDialog(
            Publication existing) {

        Dialog<Publication> dialog = new Dialog<>();

        dialog.setTitle(existing == null
                ? "Додати видання"
                : "Редагувати видання");

        ButtonType saveButton = new ButtonType(
                "Зберегти", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton, ButtonType.CANCEL
        );

        TextField title = new TextField(
                existing == null ? "" : existing.getTitle()
        );

        TextField author = new TextField(
                existing == null ? "" : existing.getAuthor()
        );

        TextField year = new TextField(
                existing == null ? "" :
                        String.valueOf(existing.getYearPublished())
        );

        TextField total = new TextField(
                existing == null ? "" :
                        String.valueOf(existing.getTotalCopies())
        );

        TextField available = new TextField(
                existing == null ? "" :
                        String.valueOf(existing.getAvailableCopies())
        );

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Назва:"), title);
        grid.addRow(1, new Label("Автор:"), author);
        grid.addRow(2, new Label("Рік:"), year);
        grid.addRow(3, new Label("Усього примірників:"), total);
        grid.addRow(4, new Label("Доступно:"), available);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                try {
                    String titleValue = title.getText().trim();
                    String authorValue = author.getText().trim();

                    if (titleValue.isEmpty() || authorValue.isEmpty()) {
                        showError("Заповніть назву та автора.");
                        return null;
                    }

                    int yearValue =
                            Integer.parseInt(year.getText().trim());

                    int totalValue =
                            Integer.parseInt(total.getText().trim());

                    int availableValue =
                            Integer.parseInt(available.getText().trim());

                    if (yearValue <= 0 || totalValue < 0 ||
                            availableValue < 0 ||
                            availableValue > totalValue) {

                        showError(
                                "Перевірте рік і кількість примірників."
                        );
                        return null;
                    }

                    return new Publication(
                            0,
                            titleValue,
                            authorValue,
                            yearValue,
                            totalValue,
                            availableValue
                    );

                } catch (NumberFormatException e) {
                    showError(
                            "Рік і кількість мають бути числами."
                    );
                }
            }

            return null;
        });

        return dialog;
    }

    // ========== CSV IMPORT ==========

    private void importPublicationsCsv() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Виберіть CSV-файл");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV файли", "*.csv"
                )
        );

        File file = fileChooser.showOpenDialog(
                publicationTable.getScene().getWindow()
        );

        if (file == null) {
            return;
        }

        PublicationCsvImportTask task =
                new PublicationCsvImportTask(
                        file,
                        publicationClient
                );

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.progressProperty().bind(
                task.progressProperty()
        );

        progressBar.setMaxWidth(Double.MAX_VALUE);

        Label status = new Label(
                "Підготовка до імпорту..."
        );

        status.textProperty().bind(
                task.messageProperty()
        );

        VBox content = new VBox(
                10,
                status,
                progressBar
        );

        content.setStyle("-fx-padding: 20;");

        Stage progressWindow = new Stage();

        progressWindow.setTitle("Імпорт видань");

        progressWindow.setScene(
                new Scene(content, 400, 120)
        );

        progressWindow.setOnCloseRequest(event -> {
            if (task.isRunning()) {
                task.cancel();
            }
        });

        task.setOnSucceeded(event -> {

            progressWindow.close();

            loadPublications();

            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Імпорт завершено! Додано видань: "
                            + task.getValue()
            ).showAndWait();
        });

        task.setOnFailed(event -> {

            progressWindow.close();

            Throwable error = task.getException();

            showError(
                    "Помилка імпорту: " +
                            (error == null
                                    ? "Невідома помилка"
                                    : error.getMessage())
            );

            loadPublications();
        });

        task.setOnCancelled(event -> {
            progressWindow.close();
            loadPublications();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        progressWindow.show();
    }

    // ========== READERS ==========


    private Tab createReaderTab() {

        addNumberColumn(readerTable, "ID",
                Reader::getId);

        addStringColumn(readerTable, "ПІБ",
                Reader::getFullName);

        addStringColumn(readerTable, "Email",
                Reader::getEmail);

        addStringColumn(readerTable, "Телефон",
                Reader::getPhoneNumber);

        addStringColumn(readerTable, "Адреса",
                Reader::getAddress);

        addStringColumn(readerTable, "Дата реєстрації",
                r -> String.valueOf(r.getRegistrationDate()));

        readerTable.setItems(readers);

        Button refresh = new Button("Оновити список");
        refresh.setOnAction(e -> loadReaders());

        Button add = new Button("Додати читача");
        add.setOnAction(e -> addReader());

        Button edit = new Button("Редагувати");
        edit.setOnAction(e -> editReader());

        Button delete = new Button("Видалити");
        delete.setOnAction(e -> deleteReader());

        HBox buttons = new HBox(
                10, refresh, add, edit, delete
        );

        return new Tab("Читачі",
                new VBox(10, readerTable, buttons));
    }


    private void loadReaders() {
        try {
            readers.setAll(readerClient.getAllReaders());
        } catch (Exception e) {
            showError("Не вдалося завантажити читачів: " + e.getMessage());
        }
    }

    private void addReader() {
        Dialog<Reader> dialog = new Dialog<>();
        dialog.setTitle("Додавання читача");
        dialog.setHeaderText("Введіть дані нового читача");

        ButtonType saveButton = new ButtonType(
                "Додати", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton, ButtonType.CANCEL
        );

        TextField nameField = new TextField();
        nameField.setPromptText("ПІБ");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Телефон");

        TextField addressField = new TextField();
        addressField.setPromptText("Адреса");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("ПІБ:"), nameField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("Телефон:"), phoneField);
        grid.addRow(3, new Label("Адреса:"), addressField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                String name = nameField.getText().trim();

                if (name.isEmpty()) {
                    showError("Введіть ПІБ читача.");
                    return null;
                }

                return new Reader(
                        0,
                        name,
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        addressField.getText().trim(),
                        null
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reader -> {
            try {
                readerClient.addReader(reader);
                loadReaders();

                new Alert(
                        Alert.AlertType.INFORMATION,
                        "Читача успішно додано!"
                ).showAndWait();

            } catch (Exception e) {
                showError("Не вдалося додати читача: " + e.getMessage());
            }
        });
    }



    private void editReader() {

        Reader selected =
                readerTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Спочатку виберіть читача.");
            return;
        }

        Dialog<Reader> dialog = new Dialog<>();
        dialog.setTitle("Редагування читача");
        dialog.setHeaderText("Змініть дані читача");

        ButtonType saveButton = new ButtonType(
                "Зберегти", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton, ButtonType.CANCEL
        );

        TextField nameField = new TextField(selected.getFullName());
        TextField emailField = new TextField(selected.getEmail());
        TextField phoneField = new TextField(selected.getPhoneNumber());
        TextField addressField = new TextField(selected.getAddress());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("ПІБ:"), nameField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("Телефон:"), phoneField);
        grid.addRow(3, new Label("Адреса:"), addressField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveButton) {

                String name = nameField.getText().trim();

                if (name.isEmpty()) {
                    showError("Введіть ПІБ читача.");
                    return null;
                }

                return new Reader(
                        selected.getId(),
                        name,
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        addressField.getText().trim(),
                        selected.getRegistrationDate()
                );
            }

            return null;
        });

        dialog.showAndWait().ifPresent(reader -> {
            try {
                readerClient.updateReader(reader);
                loadReaders();

                new Alert(
                        Alert.AlertType.INFORMATION,
                        "Дані читача успішно змінено!"
                ).showAndWait();

            } catch (Exception e) {
                showError("Не вдалося редагувати читача: "
                        + e.getMessage());
            }
        });
    }

    private void deleteReader() {

        Reader selected =
                readerTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Спочатку виберіть читача.");
            return;
        }

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Видалення читача");
        confirmation.setHeaderText(
                "Видалити читача " + selected.getFullName() + "?"
        );
        confirmation.setContentText(
                "ID читача: " + selected.getId()
        );

        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    readerClient.deleteReader(selected.getId());
                    loadReaders();

                    new Alert(
                            Alert.AlertType.INFORMATION,
                            "Читача успішно видалено!"
                    ).showAndWait();

                } catch (Exception e) {
                    showError("Не вдалося видалити читача: "
                            + e.getMessage());
                }
            }
        });
    }

    // ========== BOOKINGS ==========


    private Tab createBookingTab() {

        addNumberColumn(bookingTable, "ID",
                Booking::getId);

        addStringColumn(bookingTable, "Читач",
                b -> readerName(b.getReaderId()));

        addStringColumn(bookingTable, "Видання",
                b -> publicationName(b.getPublicationId()));

        addStringColumn(bookingTable, "Дата бронювання",
                b -> String.valueOf(b.getBookingDate()));

        addStringColumn(bookingTable, "Дата завершення",
                b -> String.valueOf(b.getExpiryDate()));

        addStringColumn(bookingTable, "Статус",
                Booking::getStatus);

        bookingTable.setItems(bookings);

        Button refresh = new Button("Оновити список");
        refresh.setOnAction(e -> loadBookings());

        Button add = new Button("Додати бронювання");
        add.setOnAction(e -> addBooking());

        Button edit = new Button("Редагувати");
        edit.setOnAction(e -> editBooking());

        Button delete = new Button("Видалити");
        delete.setOnAction(e -> deleteBooking());

        Button changeStatus = new Button("Змінити статус");
        changeStatus.setOnAction(e -> advanceBookingStatus());

        Button indicators = new Button("Інтегральний показник");
        indicators.setOnAction(e -> showBookingIndicators());

        HBox buttons = new HBox(
                10, refresh, add, edit, delete, changeStatus, indicators
        );

        return new Tab("Бронювання",
                new VBox(10, bookingTable, buttons));
    }

    private void loadBookings() {
        try {
            bookings.setAll(bookingClient.getAllBookings());
        } catch (Exception e) {
            showError("Не вдалося завантажити бронювання: " + e.getMessage());
        }
    }

    private void addBooking() {
        loadReaders();
        loadPublications();

        ComboBox<Reader> readerCombo =
                new ComboBox<>(FXCollections.observableArrayList(readers));
        configureComboBox(readerCombo, this::readerLabel);
        readerCombo.setPromptText("Вибери читача");

        ComboBox<Publication> publicationCombo =
                new ComboBox<>(FXCollections.observableArrayList(publications));
        configureComboBox(publicationCombo, this::publicationLabel);
        publicationCombo.setPromptText("Вибери видання");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        Dialog<Booking> dialog = new Dialog<>();
        dialog.setTitle("Додати бронювання");
        dialog.setHeaderText("Заповни дані бронювання");

        ButtonType save = new ButtonType(
                "Зберегти", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                save, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Читач:"), 0, 0);
        grid.add(readerCombo, 1, 0);

        grid.add(new Label("Видання:"), 0, 1);
        grid.add(publicationCombo, 1, 1);

        grid.add(new Label("Дата бронювання:"), 0, 2);
        grid.add(datePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == save) {
                if (readerCombo.getValue() == null ||
                        publicationCombo.getValue() == null ||
                        datePicker.getValue() == null) {
                    showError("Заповни всі поля.");
                    return null;
                }

                return new Booking(
                        0,
                        readerCombo.getValue().getId(),
                        publicationCombo.getValue().getId(),
                        java.sql.Date.valueOf(datePicker.getValue()),
                        null,
                        "СТВОРЕНО"
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(booking -> {
            try {
                bookingClient.addBooking(booking);
                loadBookings();
                showInfo("Бронювання успішно додано.");
            } catch (Exception e) {
                showError("Помилка додавання: " + e.getMessage());
            }
        });
    }

    private void editBooking() {
        Booking selected =
                bookingTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Спочатку вибери бронювання.");
            return;
        }

        if (!"СТВОРЕНО".equals(selected.getStatus())) {
            showError("Редагувати можна тільки створене бронювання.");
            return;
        }

        loadReaders();
        loadPublications();

        ComboBox<Reader> readerCombo =
                new ComboBox<>(FXCollections.observableArrayList(readers));
        configureComboBox(readerCombo, this::readerLabel);

        readers.stream()
                .filter(r -> r.getId() == selected.getReaderId())
                .findFirst()
                .ifPresent(readerCombo::setValue);

        ComboBox<Publication> publicationCombo =
                new ComboBox<>(FXCollections.observableArrayList(publications));
        configureComboBox(publicationCombo, this::publicationLabel);

        publications.stream()
                .filter(p -> p.getId() == selected.getPublicationId())
                .findFirst()
                .ifPresent(publicationCombo::setValue);

        LocalDate initialDate = selected.getBookingDate() == null
                ? LocalDate.now()
                : selected.getBookingDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        DatePicker datePicker = new DatePicker(initialDate);

        Dialog<Booking> dialog = new Dialog<>();
        dialog.setTitle("Редагувати бронювання");
        dialog.setHeaderText("Бронювання №" + selected.getId());

        ButtonType save = new ButtonType(
                "Зберегти", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                save, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Читач:"), 0, 0);
        grid.add(readerCombo, 1, 0);

        grid.add(new Label("Видання:"), 0, 1);
        grid.add(publicationCombo, 1, 1);

        grid.add(new Label("Дата:"), 0, 2);
        grid.add(datePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == save) {
                if (readerCombo.getValue() == null ||
                        publicationCombo.getValue() == null ||
                        datePicker.getValue() == null) {
                    showError("Заповни всі поля.");
                    return null;
                }

                return new Booking(
                        selected.getId(),
                        readerCombo.getValue().getId(),
                        publicationCombo.getValue().getId(),
                        java.sql.Date.valueOf(datePicker.getValue()),
                        selected.getExpiryDate(),
                        selected.getStatus()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(booking -> {
            try {
                bookingClient.updateBooking(booking);
                loadBookings();
                showInfo("Бронювання успішно відредаговано.");
            } catch (Exception e) {
                showError("Помилка редагування: " + e.getMessage());
            }
        });
    }

    private void deleteBooking() {
        Booking selected =
                bookingTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Спочатку вибери бронювання.");
            return;
        }

        if (!"СТВОРЕНО".equals(selected.getStatus())) {
            showError("Видаляти можна тільки створене бронювання.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Видалення бронювання");
        confirmation.setHeaderText(
                "Видалити бронювання №" + selected.getId() + "?");

        confirmation.setContentText(
                readerName(selected.getReaderId()) + " — " +
                        publicationName(selected.getPublicationId()));

        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    bookingClient.deleteBooking(selected.getId());
                    loadBookings();
                    showInfo("Бронювання успішно видалено.");
                } catch (Exception e) {
                    showError("Помилка видалення: " + e.getMessage());
                }
            }
        });
    }

    private void advanceBookingStatus() {
        Booking selected =
                bookingTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Спочатку вибери бронювання.");
            return;
        }

        String nextStatus;

        switch (selected.getStatus()) {
            case "СТВОРЕНО":
                nextStatus = "ПІДТВЕРДЖЕНО";
                break;
            case "ПІДТВЕРДЖЕНО":
                nextStatus = "ГОТОВЕ_ДО_ВИДАЧІ";
                break;
            case "ГОТОВЕ_ДО_ВИДАЧІ":
                nextStatus = "ЗАВЕРШЕНО";
                break;
            case "ЗАВЕРШЕНО":
                showError("Бронювання вже завершено.");
                return;
            default:
                showError("Невідомий статус бронювання.");
                return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Зміна статусу");
        confirmation.setHeaderText("Змінити статус бронювання №"
                + selected.getId() + "?");
        confirmation.setContentText(
                selected.getStatus() + " → " + nextStatus);

        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    bookingClient.updateBookingStatus(
                            selected.getId(), nextStatus);
                    loadBookings();
                    showInfo("Статус успішно змінено.");
                } catch (Exception e) {
                    showError("Помилка зміни статусу: " + e.getMessage());
                }
            }
        });
    }

    private void showBookingIndicators() {
        try {
            List<BookingIndicator> indicators =
                    bookingClient.getBookingIndicators();

            if (indicators.isEmpty()) {
                showInfo("Немає даних для розрахунку показників.");
                return;
            }

            TableView<BookingIndicator> table = new TableView<>();

            addNumberColumn(table, "ID бронювання",
                    BookingIndicator::getBookingId);

            addStringColumn(table, "Видання",
                    BookingIndicator::getPublicationTitle);

            addNumberColumn(table, "Очікування (дні)",
                    BookingIndicator::getWaitDays);

            addStringColumn(table, "F1",
                    i -> String.format("%.2f", i.getF1()));

            addNumberColumn(table, "Кількість бронювань",
                    BookingIndicator::getDemandCount);

            addStringColumn(table, "F2",
                    i -> String.format("%.2f", i.getF2()));

            addNumberColumn(table, "Усього примірників",
                    BookingIndicator::getTotalCopies);

            addNumberColumn(table, "Доступно примірників",
                    BookingIndicator::getAvailableCopies);

            addStringColumn(table, "F3",
                    i -> String.format("%.2f", i.getF3()));

            addStringColumn(table, "Інтегральний показник",
                    i -> String.format("%.2f", i.getIntegralValue()));

            table.setItems(
                    FXCollections.observableArrayList(indicators));

            Stage stage = new Stage();
            stage.setTitle("Інтегральні показники бронювань");
            stage.setScene(new Scene(new VBox(10, table), 1100, 500));
            stage.show();

        } catch (Exception e) {
            showError("Не вдалося отримати показники: " + e.getMessage());
        }
    }



    // ========== ISSUANCES ==========



    private Tab createIssuanceTab() {

        addNumberColumn(issuanceTable, "ID",
                Issuance::getId);

        addStringColumn(issuanceTable, "Читач",
                i -> {
                    Booking booking = bookings.stream()
                            .filter(b -> b.getId() == i.getBookingId())
                            .findFirst()
                            .orElse(null);

                    return booking == null
                            ? "Невідомий читач"
                            : readerName(booking.getReaderId());
                });

        addStringColumn(issuanceTable, "Видання",
                i -> {
                    Booking booking = bookings.stream()
                            .filter(b -> b.getId() == i.getBookingId())
                            .findFirst()
                            .orElse(null);

                    return booking == null
                            ? "Невідоме видання"
                            : publicationName(booking.getPublicationId());
                });

        addNumberColumn(issuanceTable, "ID бронювання",
                Issuance::getBookingId);

        addStringColumn(issuanceTable, "Дата видачі",
                i -> String.valueOf(i.getIssueDate()));

        addStringColumn(issuanceTable, "Дата повернення",
                i -> i.getReturnDate() == null
                        ? "Ще не повернено"
                        : String.valueOf(i.getReturnDate()));

        addStringColumn(issuanceTable, "Стан",
                i -> i.getReturnDate() == null
                        ? "ВИДАНО"
                        : "ПОВЕРНУТО");

        issuanceTable.setItems(issuances);

        Button refresh = new Button("Оновити список");
        refresh.setOnAction(e -> loadIssuances());

        Button add = new Button("Додати видачу");
        add.setOnAction(e -> addIssuance());

        Button returnBook = new Button("Повернути книгу");
        returnBook.setOnAction(e -> returnIssuance());

        HBox buttons = new HBox(
                10, refresh, add, returnBook
        );

        return new Tab("Видачі",
                new VBox(10, issuanceTable, buttons));
    }

    private void addIssuance() {
        loadBookings();
        loadReaders();
        loadPublications();
        loadIssuances();

        ObservableList<Booking> availableBookings =
                FXCollections.observableArrayList(
                        bookings.stream()
                                .filter(b -> "ГОТОВЕ_ДО_ВИДАЧІ".equals(b.getStatus()))
                                .filter(b -> issuances.stream()
                                        .noneMatch(i -> i.getBookingId() == b.getId()))
                                .toList()
                );

        if (availableBookings.isEmpty()) {
            showError("Немає бронювань, готових до видачі.");
            return;
        }

        Dialog<Issuance> dialog = new Dialog<>();
        dialog.setTitle("Додати видачу");
        dialog.setHeaderText("Вибери бронювання та дату видачі");

        ButtonType saveButtonType =
                new ButtonType("Зберегти", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(
                saveButtonType, ButtonType.CANCEL);

        ComboBox<Booking> bookingCombo = new ComboBox<>(availableBookings);
        configureComboBox(bookingCombo, this::bookingLabel);
        bookingCombo.setPromptText("Вибери бронювання");
        bookingCombo.setMaxWidth(Double.MAX_VALUE);

        DatePicker issueDatePicker = new DatePicker(LocalDate.now());
        issueDatePicker.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Бронювання:"), 0, 0);
        grid.add(bookingCombo, 1, 0);
        grid.add(new Label("Дата видачі:"), 0, 1);
        grid.add(issueDatePicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                Booking selectedBooking = bookingCombo.getValue();
                LocalDate date = issueDatePicker.getValue();

                if (selectedBooking == null) {
                    showError("Вибери бронювання.");
                    return null;
                }

                if (date == null) {
                    showError("Вкажи дату видачі.");
                    return null;
                }

                return new Issuance(
                        selectedBooking.getId(),
                        java.sql.Date.valueOf(date),
                        null
                );
            }

            return null;
        });

        dialog.showAndWait().ifPresent(issuance -> {
            try {
                issuanceClient.addIssuance(issuance);
                loadIssuances();
                loadBookings();
                loadPublications();
                showInfo("Видачу успішно додано.");
            } catch (Exception e) {
                showError("Помилка додавання видачі: " + e.getMessage());
            }
        });
    }
    private void loadIssuances() {
        try {
            // Спочатку оновлюємо бронювання,
            // щоб підтягнути актуальні дані читачів і видань
            loadBookings();

            issuances.setAll(
                    issuanceClient.getAllIssuances()
            );

            issuanceTable.refresh();

        } catch (Exception e) {
            showError("Не вдалося завантажити видачі: "
                    + e.getMessage());
        }
    }


    private void returnIssuance() {

        Issuance selected =
                issuanceTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Спочатку виберіть видачу.");
            return;
        }

        if (selected.getReturnDate() != null) {
            showError("Цю книгу вже повернуто.");
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Повернення книги");
        confirmation.setHeaderText(
                "Повернути книгу за видачею №" + selected.getId() + "?"
        );

        confirmation.setContentText(
                "Буде встановлено поточну дату повернення."
        );

        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    issuanceClient.returnIssuance(selected.getId());
                    loadIssuances();

                    new Alert(
                            Alert.AlertType.INFORMATION,
                            "Книгу успішно повернуто!"
                    ).showAndWait();

                } catch (Exception e) {
                    showError("Не вдалося повернути книгу: "
                            + e.getMessage());
                }
            }
        });
    }


    // ========== TABLE HELPERS ==========

    // ========== COMBO BOX HELPERS ==========

    private <T> void configureComboBox(
            ComboBox<T> combo,
            Function<T, String> labelFunction) {

        combo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : labelFunction.apply(item));
            }
        });

        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : labelFunction.apply(item));
            }
        });
    }

    private String readerLabel(Reader reader) {
        return reader.getFullName() + " — " + reader.getEmail();
    }

    private String publicationLabel(Publication publication) {
        return publication.getTitle() + " — " + publication.getAuthor();
    }

    private String readerName(int readerId) {
        return readers.stream()
                .filter(r -> r.getId() == readerId)
                .map(Reader::getFullName)
                .findFirst()
                .orElse("Читач #" + readerId);
    }

    private String publicationName(int publicationId) {
        return publications.stream()
                .filter(p -> p.getId() == publicationId)
                .map(Publication::getTitle)
                .findFirst()
                .orElse("Видання #" + publicationId);
    }

    private String bookingLabel(Booking booking) {
        return "Бронювання #" + booking.getId()
                + " — " + readerName(booking.getReaderId())
                + " — " + publicationName(booking.getPublicationId());
    }


    private <T> void addNumberColumn(
            TableView<T> table,
            String title,
            Function<T, Integer> getter) {

        TableColumn<T, Number> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        getter.apply(data.getValue())
                )
        );

        table.getColumns().add(column);
    }

    private <T> void addStringColumn(
            TableView<T> table,
            String title,
            Function<T, String> getter) {

        TableColumn<T, String> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data ->
                new SimpleStringProperty(
                        getter.apply(data.getValue())
                )
        );

        table.getColumns().add(column);
    }

    // ========== ERRORS ==========

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Інформація");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

