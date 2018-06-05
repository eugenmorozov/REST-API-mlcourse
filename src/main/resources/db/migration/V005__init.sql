CREATE TABLE IF NOT EXISTS votes (
  nickname CITEXT COLLATE "ucs_basic" REFERENCES  "users"(nickname) ON DELETE CASCADE,
  user_id INTEGER NOT NULL REFERENCES  "users"(id) ON DELETE CASCADE,
  thread INTEGER NOT NULL REFERENCES "threads"(id) ON DELETE CASCADE,
  voice INTEGER DEFAULT 0,
  UNIQUE (nickname, thread)
);