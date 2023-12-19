DROP DATABASE if exists DoodleDB;
CREATE DATABASE DoodleDB;
USE DoodleDB;
CREATE TABLE UserInfo (
    UID int primary key not null auto_increment,
    Username varchar(50) NOT NULL unique,
    Password varchar(50) NOT NULL,
    Email varchar(100) NOT NULL unique,
    Nickname varchar(30) NOT NULL,
    Theme varchar(20) NOT NULL,
	Score int NOT NULL
); 