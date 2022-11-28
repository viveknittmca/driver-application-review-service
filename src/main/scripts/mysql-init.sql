DROP DATABASE IF EXISTS applicationreviewservice;
DROP USER IF EXISTS `application_review_service`@`%`;
CREATE DATABASE IF NOT EXISTS applicationreviewservice CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS `application_review_service`@`%` IDENTIFIED WITH mysql_native_password BY 'password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, EXECUTE, CREATE VIEW, SHOW VIEW,
CREATE ROUTINE, ALTER ROUTINE, EVENT, TRIGGER ON `applicationreviewservice`.* TO `application_review_service`@`%`;
FLUSH PRIVILEGES;