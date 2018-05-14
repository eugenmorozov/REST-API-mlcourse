package project.services;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;

import javafx.geometry.Pos;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import project.models.ForumModel;
import project.models.PostModel;
import project.models.ThreadModel;

@Repository
public class PostService {
    private JdbcTemplate jdbcTemplate;

    public PostService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PostModel getPostById(int id){
        try {
            String getQuery = "select * from posts where id = ?";
            return jdbcTemplate.queryForObject(getQuery, new Object[]{id}, new PostService.PostRowMapper());
        }catch(DataAccessException error){
            return null;
        }
    }

    @Transactional
    public List<PostModel> CreatePosts(List<PostModel> posts, ThreadModel thread){

        String createQuery = "INSERT INTO posts (author, created, forum, isEdited, message, parent, thread ) VALUES (?, ?::TIMESTAMPTZ, ?, ?, ?, ?, ?)";
        String currentTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        for(PostModel post : posts) {
                if( post.getParent() != 0  && getPostById( post.getParent() ) == null){
                    throw new RuntimeException();
                } else {
                    post.setCreated(currentTime);
                    post.setEdited(false);
                    post.setForum(thread.getForum());
                    post.setThread(thread.getId());
                    jdbcTemplate.update(
                            createQuery,
                            post.getAuthor(),
                            post.getCreated(),
                            post.getForum(),
                            post.isEdited(),
                            post.getMessage(),
                            post.getParent(),
                            post.getThread()
                    );
                }
            }
            return posts;
    }

    public PostModel postDetails(
            int id,
            List<String> related
    ){
        PostModel post = getPostById(id);
        for( String keyword : related){
            if(keyword.trim().toLowerCase().contains("forum")){
                System.out.println("contains, yep");
            }
        }
        return post;
    }

    public PostModel updatePost(
            PostModel post,
            PostModel oldPost
    ){
        String createQuery = "UPDATE posts SET ( author, created, forum, isEdited, message) = (?,?::TIMESTAMPTZ,?,?,?) WHERE id = ?";
        if(post.getAuthor() != null){
            oldPost.setAuthor(post.getAuthor());
        }
        if(post.getCreated() != null){
            oldPost.setCreated(post.getCreated());
        }
        if (post.getForum() != null){
            oldPost.setForum(post.getForum());
        }
        if ( post.getMessage() != null){
            oldPost.setMessage(post.getMessage());
            oldPost.setEdited(true);
        }

        jdbcTemplate.update(
                createQuery,
                oldPost.getAuthor(),
                oldPost.getCreated(),
                oldPost.getForum(),
                oldPost.isEdited(),
                oldPost.getMessage(),
                oldPost.getId()
        );
        return oldPost;
    }

    public static class PostRowMapper implements RowMapper<PostModel> {
        @Override
        public PostModel mapRow(ResultSet resSet, int rowNum) throws SQLException {
            return new PostModel(
                    resSet.getString("author"),
                    resSet.getString("created"),
                    resSet.getString("forum"),
                    resSet.getInt("id"),
                    resSet.getBoolean("isEdited"),
                    resSet.getString("message"),
                    resSet.getInt("parent"),
                    resSet.getInt("thread")
            );
        }
    }
}
