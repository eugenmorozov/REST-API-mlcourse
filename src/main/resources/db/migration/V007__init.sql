CREATE INDEX users_nickname_idx ON users ((lower(nickname)));
CREATE INDEX  posts_id_idx ON posts (id);
CREATE INDEX  posts_thread_path_idx ON posts(thread, path);
CREATE INDEX  forum_users_forum_nickname_idx ON forum_users(forum, nickname);
CREATE INDEX  threads_forum_created_idx ON threads(forum, created);
CREATE INDEX  votes_thread_nickname_idx ON votes(thread, nickname);