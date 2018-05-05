drop table if exists tasks cascade;
create extension if not exists citext;

CREATE TABLE IF NOT EXISTS users (
  id SERIAL NOT NULL PRIMARY KEY,
  about TEXT DEFAULT NULL,
  fullname TEXT DEFAULT NULL,
  nickname CITEXT COLLATE "ucs_basic" UNIQUE,
  email CITEXT UNIQUE
);
