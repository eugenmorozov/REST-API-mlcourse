CREATE TABLE IF NOT EXISTS forum_users (
  about TEXT DEFAULT NULL,
  fullname TEXT DEFAULT NULL,
  nickname CITEXT COLLATE "ucs_basic" REFERENCES  "users"(nickname) ON DELETE CASCADE,
  email CITEXT,
  forum CITEXT COLLATE "ucs_basic" REFERENCES "forums"(slug) ON DELETE CASCADE
);