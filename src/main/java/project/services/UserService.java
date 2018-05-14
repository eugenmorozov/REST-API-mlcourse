package project.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import project.models.UserModel;

@Repository
public class UserService {

    private JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createUser(UserModel userModel) {
        String createQuery = "INSERT INTO users (about, email, fullname, nickname) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(
                createQuery,
                userModel.getAbout(),
                userModel.getEmail(),
                userModel.getFullname(),
                userModel.getNickname()
        );
    }

    public ArrayList<UserModel> getSameUsers(String nickname,String email){
        String getQuery = "select * from users where nickname = ? or email = ?";
        return (ArrayList<UserModel>) jdbcTemplate.query(getQuery, new Object[]{nickname,email}, new UserRowMapper());
    }

    public ArrayList<UserModel> getUsersByThreadAndPost(String slug,
                                                        int limit,
                                                        int since,
                                                        boolean desc){
        String getQuery = "SELECT about, email, fullname, nickname FROM users JOIN threads ON users.nickname = threads.author WHERE threads.forum = ?::citext " +
                "AND users.id > ?";
        getQuery += " UNION ";
        getQuery += " SELECT about, email, fullname, nickname FROM users JOIN posts ON users.nickname = posts.author WHERE posts.forum = ?::citext " +
                "AND users.id > ?";
        getQuery += " ORDER BY nickname";
        if(desc){
            getQuery += " DESC ";
        }
        getQuery += " LIMIT ? ";
        return (ArrayList<UserModel>) jdbcTemplate.query(getQuery,new Object[]{slug, since, slug, since, limit}, new UserRowMapper());
    }


    public UserModel getUserByNickname(String nickname){
        try {
            String getQuery = "select * from users where nickname = ?::citext";
            return jdbcTemplate.queryForObject(getQuery, new Object[]{nickname}, new UserRowMapper());
        }catch(DataAccessException error){
            return null;
        }
    }

    public UserModel updateUserByNickname(UserModel user, UserModel oldUser){
   
        String createQuery = "UPDATE users SET (about, email, fullname, nickname) = (?, ?, ?, ?) WHERE nickname = ?";
        if(user.getAbout() != null){
            oldUser.setAbout(user.getAbout() );
        }
        if(user.getEmail() != null){
            oldUser.setEmail(user.getEmail() );
        }
        if(user.getFullname() != null){
            oldUser.setFullname(user.getFullname() );
        }
        if(user.getNickname() != null){
            oldUser.setNickname(user.getNickname() );
        }
        jdbcTemplate.update(
                createQuery,
                oldUser.getAbout(),
                oldUser.getEmail(),
                oldUser.getFullname(),
                oldUser.getNickname(),
                oldUser.getNickname()
        );
        return oldUser;
    }


    public static class UserRowMapper implements RowMapper<UserModel> {
        @Override
        public UserModel mapRow(ResultSet resSet, int rowNum) throws SQLException {
            return new UserModel(
                    resSet.getString("about"),
                    resSet.getString("email"),
                    resSet.getString("fullname"),
                    resSet.getString("nickname")
            );
        }
    }
}