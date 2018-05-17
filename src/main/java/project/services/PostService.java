package project.services;

import java.io.IOException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;

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

    private Array getPathById(Integer id) {
        String sql = "SELECT path FROM posts WHERE id = ?";
        return  jdbcTemplate.queryForObject(sql, Array.class, id);
    }

    private Integer generateId(){
        return jdbcTemplate.
            queryForObject("SELECT nextval(pg_get_serial_sequence('posts', 'id'))", Integer.class);
    }

    @Transactional
    public List<PostModel> CreatePosts(List<PostModel> posts, ThreadModel thread){

        String createQuery = "INSERT INTO posts (author, created, forum, id, isEdited, message, parent, path, thread )" +
                " VALUES (?, ?::TIMESTAMPTZ, ?, ?, ?, ?, ?, array_append(?, ?::INTEGER), ?)";
        String currentTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        for(PostModel post : posts) {
            Array path = null;
            PostModel parentPost = getPostById( post.getParent() );
            int id = generateId();
            if( post.getParent() != 0  && parentPost == null){
                throw new RuntimeException();
            } else {

                if(post.getParent() != 0) {
                    path = getPathById(parentPost.getId());
                }
                post.setCreated(currentTime);
                post.setForum(thread.getForum());
                post.setEdited(false);
                post.setThread(thread.getId());
                jdbcTemplate.update(
                        createQuery,
                        post.getAuthor(),
                        post.getCreated(),
                        post.getForum(),
                        id,
                        post.isEdited(),
                        post.getMessage(),
                        post.getParent(),
                        path,
                        id,
                        post.getThread()
                );
            }
        }
        jdbcTemplate.update(
                "UPDATE forums SET posts = forums.posts + ? WHERE slug = ?::citext",
                posts.size(),
                thread.getForum()
        );
        return posts;
    }

    public ArrayList<PostModel> getPosts(int id,
                                    int limit,
                                    int since,
                                    String sort,
                                    boolean desc) {
        //we have three ways of sort:
        //
        //select *  from posts where path[1] in
        //(select distinct path[1] from posts where path[1] > since(0) order by path[1]  limit n)
        // order by path[1] (desc) , path;
        //
        //select *  from posts where path[1] > since(0) order by path[1] (desc), path limit n
        //
        //select * from posts where id > since(0) order by id (desc) limit n
        if(sort.equals("tree")){
            String getQuery = "SELECT author, created, forum, id, isedited, message, parent, thread FROM posts " +
                    " WHERE thread = ? and path[1] > ? order by path[1]";
            if(desc){
                getQuery += " DESC ";
            }
            getQuery += " ,path limit ? ";
            return (ArrayList<PostModel>) jdbcTemplate.query(getQuery,new Object[]{id, since, limit}, new PostRowMapper());
        }else if(sort.equals("parent_tree")){
            String getQuery = "SELECT author, created, forum, id, isedited, message, parent, thread FROM posts " +
                    " where thread = ? and path[1] in" +
                    " (select distinct path[1] from posts where path[1] > ? order by path[1]  limit ?) order by path[1] ";
            if(desc){
                getQuery += " DESC ";
            }
            getQuery += " , path ";
            return (ArrayList<PostModel>) jdbcTemplate.query(getQuery,new Object[]{id, since, limit}, new PostRowMapper());
        }

        String getQuery = "SELECT author, created, forum, id, isedited, message, parent, thread FROM posts WHERE thread = ? and path[1] > ? order by id ";
        if(desc){
            getQuery += " DESC ";
        }
        getQuery += " LIMIT ? ";
        return (ArrayList<PostModel>) jdbcTemplate.query(getQuery,new Object[]{id, since, limit}, new PostRowMapper());

    }

//    public PostModel postDetails(
//            int id,
//            List<String> related
//    ){
//        PostModel post = getPostById(id);
//        for( String keyword : related){
//            if(keyword.trim().toLowerCase().contains("forum")){
//                System.out.println("contains, yep");
//            }
//        }
//        return post;
//    }

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
