package com.library;

import com.library.dao.ReaderDAO;
import com.library.model.Reader;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Перевірка роботи DAO шару...");

        ReaderDAO readerDAO = new ReaderDAO();
        List<Reader> readers = readerDAO.getAllReaders();

        System.out.println("Кількість читачів у базі: " + readers.size());
        for (Reader reader : readers) {
            System.out.println("- " + reader.getFullName());
        }
    }
}