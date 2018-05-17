CREATE TABLE IF NOT EXISTS votes (
  nickname CITEXT COLLATE "ucs_basic" REFERENCES  "users"(nickname) ON DELETE CASCADE,
  thread CITEXT COLLATE "ucs_basic" REFERENCES "threads"(slug) ON DELETE CASCADE,
  voice INTEGER DEFAULT 0,
  UNIQUE (nickname, thread)
);