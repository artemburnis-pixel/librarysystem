-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `library_system`
--

-- --------------------------------------------------------

--
-- Table structure for table `bookings`
--

CREATE TABLE `bookings` (
                            `id` int(11) NOT NULL,
                            `reader_id` int(11) NOT NULL,
                            `publication_id` int(11) NOT NULL,
                            `status` enum('СТВОРЕНО','ПІДТВЕРДЖЕНО','ГОТОВЕ_ДО_ВИДАЧІ','ЗАВЕРШЕНО') DEFAULT 'СТВОРЕНО',
                            `booking_date` timestamp NOT NULL DEFAULT current_timestamp(),
                            `expiry_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`id`, `reader_id`, `publication_id`, `status`, `booking_date`, `expiry_date`) VALUES
                                                                                                          (1, 1, 1, 'ПІДТВЕРДЖЕНО', '2026-10-17 21:35:10', '2026-10-20'),
                                                                                                          (2, 2, 2, 'СТВОРЕНО', '2026-10-17 21:35:10', '2026-10-20');

-- --------------------------------------------------------

--
-- Table structure for table `issuances`
--

CREATE TABLE `issuances` (
                             `id` int(11) NOT NULL,
                             `booking_id` int(11) NOT NULL,
                             `issue_date` timestamp NOT NULL DEFAULT current_timestamp(),
                             `return_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `publications`
--

CREATE TABLE `publications` (
                                `id` int(11) NOT NULL,
                                `title` varchar(255) NOT NULL,
                                `author` varchar(255) NOT NULL,
                                `year` int(11) NOT NULL,
                                `total_copies` int(11) NOT NULL,
                                `available_copies` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `publications`
--

INSERT INTO `publications` (`id`, `title`, `author`, `year`, `total_copies`, `available_copies`) VALUES
                                                                                                     (1, 'Програмування на Java', 'Брюс Еккель', 2020, 5, 3),
                                                                                                     (2, 'Алгоритми і структури даних', 'Томас Кормен', 2018, 4, 2),
                                                                                                     (3, 'Чистий код', 'Роберт Мартін', 2015, 6, 4);

-- --------------------------------------------------------

--
-- Table structure for table `readers`
--

CREATE TABLE `readers` (
                           `id` int(11) NOT NULL,
                           `full_name` varchar(255) NOT NULL,
                           `email` varchar(100) NOT NULL,
                           `phone` varchar(20) NOT NULL,
                           `address` varchar(255) DEFAULT NULL,
                           `registration_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `readers`
--

INSERT INTO `readers` (`id`, `full_name`, `email`, `phone`, `address`, `registration_date`) VALUES
                                                                                                (1, 'Бурніс Артем Євгенійович', 'artem.burnis@nup.edu.ua', '+380501112233', NULL, NULL),
                                                                                                (2, 'Петренко Іван Олександрович', 'petrenko@gmail.com', '+380672223344', NULL, NULL);

--
-- Indexes for dumped tables
--

ALTER TABLE `bookings`
    ADD PRIMARY KEY (`id`),
  ADD KEY `fk_booking_reader` (`reader_id`),
  ADD KEY `fk_booking_publication` (`publication_id`);

ALTER TABLE `issuances`
    ADD PRIMARY KEY (`id`),
  ADD KEY `fk_issuance_booking` (`booking_id`);

ALTER TABLE `publications`
    ADD PRIMARY KEY (`id`);

ALTER TABLE `readers`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

ALTER TABLE `bookings`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

ALTER TABLE `issuances`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `publications`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

ALTER TABLE `readers`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

ALTER TABLE `bookings`
    ADD CONSTRAINT `fk_booking_publication` FOREIGN KEY (`publication_id`) REFERENCES `publications` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_booking_reader` FOREIGN KEY (`reader_id`) REFERENCES `readers` (`id`) ON DELETE CASCADE;

ALTER TABLE `issuances`
    ADD CONSTRAINT `fk_issuance_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;