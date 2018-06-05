package project.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import project.models.ForumModel;

@Repository
public class ForumService {
    private JdbcTemplate jdbcTemplate;

    public ForumService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ForumModel createForum(ForumModel forum){
         return jdbcTemplate.queryForObject(
            "INSERT INTO forums (posts, slug, threads, title, nickname) VALUES (?,?::citext,?,?,?::citext) RETURNING *",
            new ForumRowMapper(),
            forum.getPosts(),
            forum.getSlug(),
            forum.getThreads(),
            forum.getTitle(),
            forum.getUser()
        );
    }

    public ForumModel getForumBySlug(String slug){
        try {
            String getQuery = "select posts, slug, threads, title, nickname from forums where slug = ?::citext";
            return jdbcTemplate.queryForObject(getQuery, new Object[]{slug}, new ForumService.ForumRowMapper());
        }catch(DataAccessException error){
            return null;
        }
    }

    public static class ForumRowMapper implements RowMapper<ForumModel> {
        @Override
        public ForumModel mapRow(ResultSet resSet, int rowNum) throws SQLException {
            return new ForumModel(
                    resSet.getInt("posts"),
                    resSet.getString("slug"),
                    resSet.getInt("threads"),
                    resSet.getString("title"),
                    resSet.getString("nickname")
            );
        }
    }
}
