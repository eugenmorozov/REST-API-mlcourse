package project.services;

import project.models.ServiceModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ServiceService {
    private JdbcTemplate jdbcTemplate;

    public ServiceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clearAll(){
        jdbcTemplate.update(
                "TRUNCATE forums, forum_users, users, threads, posts, votes"
        );
    }

    public ServiceModel getInfo(){
        int forums = jdbcTemplate.queryForObject("SELECT COUNT(*) from forums",int.class);
        int posts = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM posts", int.class);
        int threads = jdbcTemplate.queryForObject("SELECT COUNT(*) from threads",int.class);
        int users = jdbcTemplate.queryForObject("SELECT COUNT(*) from users",int.class);
        return new ServiceModel(forums,posts,threads,users);
    }

}
