create extension if not exists citext;
CREATE TABLE IF NOT EXISTS posts (
  author CITEXT COLLATE "ucs_basic" REFERENCES "users"(nickname) ON DELETE CASCADE,
  created TIMESTAMPTZ,
  forum CITEXT COLLATE "ucs_basic" REFERENCES "forums"(slug) ON DELETE CASCADE,
  id SERIAL NOT NULL PRIMARY KEY,
  isEdited BOOLEAN DEFAULT FALSE,
  message TEXT DEFAULT '',
  parent INTEGER NOT NULL DEFAULT 0,
  path INTEGER [],
  thread INTEGER NOT NULL REFERENCES "threads"(id) ON DELETE CASCADE
);
