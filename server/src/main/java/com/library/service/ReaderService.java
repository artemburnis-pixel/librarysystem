
package com.library.service;

import com.library.dao.ReaderDAO;
import com.library.model.Reader;

import java.util.List;

public class ReaderService {

    private final ReaderDAO readerDAO;

    public ReaderService() {
        this.readerDAO = new ReaderDAO();
    }

    public List<Reader> getAllReaders() {
        return readerDAO.getAllReaders();
    }

    public void addReader(Reader reader) {
        validateReader(reader);
        readerDAO.addReader(reader);
    }

    public void updateReader(Reader reader) {
        validateReader(reader);

        if (reader.getId() <= 0) {
            throw new IllegalArgumentException("Некоректний ID читача");
        }

        if (!readerDAO.updateReader(reader)) {
            throw new IllegalArgumentException("Читача не знайдено");
        }
    }

    public void deleteReader(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Некоректний ID читача");
        }

        if (!readerDAO.deleteReader(id)) {
            throw new IllegalArgumentException("Читача не знайдено");
        }
    }

    private void validateReader(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("Читач не може бути порожнім");
        }

        if (reader.getFullName() == null ||
                reader.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Введіть ПІБ читача");
        }
    }
}
