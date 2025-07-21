--
-- Datenbank-Schema für das FormPilot-Projekt
-- Stand: 21.07.2025
--

--
-- Tabelle für die Berater-Konten
--
CREATE TABLE `berater` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Tabelle für die gespeicherten Erfassungsbögen
--
CREATE TABLE `formulare` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `berater_id` int(11) NOT NULL,
  `companyName` varchar(255) DEFAULT NULL,
  `legalForm` varchar(255) DEFAULT NULL,
  `formData` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `berater_id` (`berater_id`),
  CONSTRAINT `formulare_ibfk_1` FOREIGN KEY (`berater_id`) REFERENCES `berater` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;