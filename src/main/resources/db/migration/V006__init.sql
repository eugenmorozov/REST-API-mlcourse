CREATE TABLE IF NOT EXISTS forum_users (
        about TEXT DEFAULT NULL,
        fullname TEXT DEFAULT NULL,
        nickname CITEXT COLLATE "ucs_basic",
        email CITEXT,
        forum CITEXT COLLATE "ucs_basic",
        UNIQUE (nickname, forum)
);


