        CREATE INDEX users_helpful_idx ON users ((lower(nickname)));
        CREATE INDEX posts_helpful_idx ON posts (id);
        CREATE INDEX  posts_helpful_idx ON posts(thread, path);

