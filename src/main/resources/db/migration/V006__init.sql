        CREATE INDEX users_helpful_idx ON users ((lower(nickname)));
        CREATE INDEX  posts_id_idx ON posts (id);
        CREATE INDEX  posts_helpful_idx ON posts(thread, path);
        CREATE INDEX  users_forums_helpful_idx ON forum_users(forum, nickname);
        CREATE INDEX  threads_search_idx ON threads(forum, created);

