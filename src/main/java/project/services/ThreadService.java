package project.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import project.models.ThreadModel;

@Repository
public class ThreadService {
    private JdbcTemplate jdbcTemplate;

    public ThreadService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createThread(ThreadModel thread) {
        jdbcTemplate.update(
                "INSERT INTO threads (author, created, forum, message, slug, title, votes) VALUES (?, ?, ?, ?, ?, ?, ?)",
                thread.getAuthor(),
                thread.getCreated(),
                thread.getForum(),
                thread.getMessage(),
                thread.getSlug(),
                thread.getTitle(),
                thread.getVotes()
        );
    }

    public ThreadModel getThreadBySlug(String slug){
        try {
            String getQuery = "select * from threads where slug = ?::citext";
            return jdbcTemplate.queryForObject(getQuery, new Object[]{slug}, new ThreadService.ThreadRowMapper());
        }catch(DataAccessException error){
            return null;
        }
    }

    public ArrayList<ThreadModel> getThreadsBySlug(String slug){
        try {
            return (ArrayList<ThreadModel>) jdbcTemplate.query(
                    "select * from threads where forum = ?::citext",
                    new Object[]{slug},
                    new ThreadRowMapper()
            );
        }catch (DataAccessException error){
            return null;
        }
    }

    public static class ThreadRowMapper implements RowMapper<ThreadModel> {
        @Override
        public ThreadModel mapRow(ResultSet resSet, int rowNum) throws SQLException {
            return new ThreadModel(
                    resSet.getString("author"),
                    resSet.getTimestamp("created"),
                    resSet.getString("forum"),
                    resSet.getInt("id"),
                    resSet.getString("message"),
                    resSet.getString("slug"),
                    resSet.getString("title"),
                    resSet.getInt("votes")
            );
        }
    }

}
