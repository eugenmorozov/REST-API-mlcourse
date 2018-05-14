package project.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

    public ArrayList<ThreadModel> getThreadsBySlug(
            String slug,
            int limit,
            Timestamp since,
            boolean desc
    ){
        String query ="SELECT * FROM threads WHERE forum = ?::citext AND created > ? ORDER BY created ";
        if(desc){
            query += " DESC ";
        }
        query += " LIMIT ? ";
        return (ArrayList<ThreadModel>) jdbcTemplate.query(
                query,
                new Object[]{slug, since,limit},
                new ThreadRowMapper()
        );

    }

    public ThreadModel getThreadBySlugOrId(
            String slugOrId
    ){
        try {
            if(slugOrId.matches("\\d+") ) {
                return jdbcTemplate.queryForObject(
                        "select * from threads where  id = ?",
                        new Object[]{Integer.parseInt(slugOrId)},
                        new ThreadService.ThreadRowMapper()
                );
            }else{
                return jdbcTemplate.queryForObject(
                        "select * from threads where  slug = ?::citext",
                        new Object[]{slugOrId},
                        new ThreadService.ThreadRowMapper()
                );
            }
        }catch(DataAccessException error){
            return null;
        }

    }

    public ThreadModel updateThread( ThreadModel thread, ThreadModel oldThread){
        String createQuery = "UPDATE threads SET ( author, created, forum, message, slug, title) = (?,?,?,?,?,?) WHERE slug = ?::citext";
        if ( thread.getAuthor() != null){
            oldThread.setAuthor(thread.getAuthor());
        }
        if ( thread.getCreated() != null){
            oldThread.setCreated(thread.getCreated());
        }
        if ( thread.getForum() != null){
            oldThread.setForum(thread.getForum());
        }
        if ( thread.getMessage() != null){
            oldThread.setMessage(thread.getMessage());
        }
        if ( thread.getSlug() != null){
            oldThread.setSlug(thread.getSlug());
        }
        if ( thread.getTitle() != null){
            oldThread.setTitle(thread.getTitle());
        }
        jdbcTemplate.update(
                createQuery,
                oldThread.getAuthor(),
                oldThread.getCreated(),
                oldThread.getForum(),
                oldThread.getMessage(),
                oldThread.getSlug(),
                oldThread.getTitle(),
                oldThread.getSlug()
        );
        return oldThread;

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
