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

    public UserModel getUserByNickname(String nickname){
        try {
            String getQuery = "select * from users where nickname = ?";
            return jdbcTemplate.queryForObject(getQuery, new Object[]{nickname}, new UserRowMapper());
        }catch(DataAccessException error){
            return null;
        }
    }

    public UserModel updateUserByNickname(UserModel user){
   
        String createQuery = "UPDATE users SET (about, email, fullname, nickname) = (?, ?, ?, ?) WHERE nickname = ?";

        jdbcTemplate.update(
                createQuery,
                user.getAbout(),
                user.getEmail(),
                user.getFullname(),
                user.getNickname(),
                user.getNickname()
        );
        return user;
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