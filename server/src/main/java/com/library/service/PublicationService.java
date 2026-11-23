package com.library.service;

import com.library.dao.PublicationDAO;
import com.library.model.Publication;

import java.util.List;
import java.util.logging.Logger;

public class PublicationService {

    private static final Logger LOGGER =
            Logger.getLogger(PublicationService.class.getName());

    private final PublicationDAO publicationDAO;

    public PublicationService() {
        this(new PublicationDAO());
    }

    public PublicationService(PublicationDAO publicationDAO) {
        this.publicationDAO = publicationDAO;
    }

    public List<Publication> getAllPublications() {
        return publicationDAO.getAllPublications();
    }

    public void addPublication(Publication publication) {
        validatePublication(publication);
        publicationDAO.addPublication(publication);
        LOGGER.info("Додано нове видання: " + publication.getTitle());
    }

    public void updatePublication(Publication publication) {
        if (publication == null || publication.getId() <= 0) {
            throw new IllegalArgumentException("Некоректний ID видання");
        }

        validatePublication(publication);
        publicationDAO.updatePublication(publication);
        LOGGER.info("Оновлено видання ID=" + publication.getId());
    }

    public void deletePublication(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Некоректний ID видання");
        }

        publicationDAO.deletePublication(id);
        LOGGER.info("Видалено видання ID=" + id);
    }

    private void validatePublication(Publication publication) {
        if (publication == null) {
            throw new IllegalArgumentException("Видання не може бути порожнім");
        }

        if (publication.getTitle() == null ||
                publication.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Введіть назву видання");
        }

        if (publication.getAuthor() == null ||
                publication.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Введіть автора");
        }

        if (publication.getYearPublished() <= 0) {
            throw new IllegalArgumentException("Некоректний рік видання");
        }

        if (publication.getTotalCopies() < 0 ||
                publication.getAvailableCopies() < 0) {
            throw new IllegalArgumentException("Кількість примірників не може бути від'ємною");
        }

        if (publication.getAvailableCopies() > publication.getTotalCopies()) {
            throw new IllegalArgumentException(
                    "Доступних примірників не може бути більше загальної кількості"
            );
        }
    }
}