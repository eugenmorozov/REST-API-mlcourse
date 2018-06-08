package project.services;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;
import project.models.PostFullModel;
import project.models.PostModel;
import project.models.ThreadModel;
import project.models.UserModel;

@Repository
public class PostService {
    private JdbcTemplate jdbcTemplate;
    private ThreadService threadService;
    private UserService userService;
    private ForumService forumService;

    public PostService(JdbcTemplate jdbcTemplate,
                       ThreadService threadService,
                       UserService userService,
                       ForumService forumService) {
        this.jdbcTemplate = jdbcTemplate;
        this.threadService = threadService;
        this.userService = userService;
        this.forumService = forumService;
    }

    public PostModel getPostById(int id){
        try {
            String getQuery = "select author, created, forum, id, isedited, message, parent, thread from posts where id = ?";
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
                " VALUES (?, ?::TIMESTAMPTZ, ?, ?, ?, ?, ?, array_append(?, ?::INTEGER), ?) ";
        String currentTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

        for(PostModel post : posts) {
            UserModel user = userService.getUserByNickname(post.getAuthor());
            if(user == null){
                return null;
            }
            jdbcTemplate.update(
                    "INSERT INTO forum_users (about, fullname,nickname, email, forum) VALUES (?,?,?,?,?)",
                    user.getAbout(),
                    user.getFullname(),
                    user.getNickname(),
                    user.getEmail(),
                    thread.getForum()
            );
            Array path = null;
            PostModel parentPost = getPostById( post.getParent() );
            int id = generateId();//TODO убрать нахуй!
            if( post.getParent() != 0  && parentPost == null){
                throw new RuntimeException();
            } else {
                if (post.getParent() != 0 && parentPost != null && parentPost.getThread() != thread.getId()){
                    throw new RuntimeException();
                }
                if(post.getParent() != 0) {
                    path = getPathById(parentPost.getId());
                }
                post.setCreated(currentTime);
                post.setForum(thread.getForum());
                post.setIsEdited(false);
                post.setThread(thread.getId());
                post.setId(id);//TODO: YBRAT NAXYI
                jdbcTemplate.update(
                        createQuery,
                        post.getAuthor(),
                        post.getCreated(),
                        post.getForum(),
                        post.getId(),
                        post.getIsEdited(),
                        post.getMessage(),
                        post.getParent(),
                        path,
                        post.getId(),
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
        if(sort.equals("tree")){
            String getQuery = "SELECT author, created, forum, id, isedited, message, parent, thread FROM posts " +
                    " WHERE thread = ? ";
            if(desc){
                if(since != 0 && since != 999999){
                    getQuery += "and path < (select path from posts where id  = ?)";
                }else{
                    getQuery += "and path[1] < ? ";
                }
                getQuery+= "order by path DESC ";
            }else{
                if(since != 0 && since != 999999){
                    getQuery += "and path > (select path from posts where id  = ?)";
                }else{
                    getQuery += "and path[1] > ? ";
                }
                 getQuery += " order by path";
            }
            getQuery += " limit ? ";
            System.out.println(getQuery);
            return (ArrayList<PostModel>) jdbcTemplate.query(getQuery,new Object[]{id, since, limit}, new PostRowMapper());

        }else if(sort.equals("parent_tree")){
            String getQuery = "SELECT author, created, forum, id, isedited, message, parent, thread FROM posts " +
                    " where thread = ? and path[1] in (select distinct path[1] from posts ";
            if(desc){
                if(since != 0 && since != 999999) {
                    getQuery += "where thread = ? and path[1] < (select path[1] from posts where id = ?) order by path[1] desc limit ?) order by path[1]  DESC ";
                }else{
                    getQuery += "where thread = ? and path[1] <  ? order by path[1] desc limit ?) order by path[1]  DESC ";
                }
            }else{
                if(since != 0 && since != 999999) {
                    getQuery += " where thread = ? and path[1] > (select path[1] from posts where id = ?) order by path[1] limit ?) order by path[1] ";
                }else{
                    getQuery += "where thread = ? and path[1] > ? order by path[1] limit ?) order by path[1]  ";
                }
            }
            getQuery += " , path ";
            System.out.println(getQuery);
            return (ArrayList<PostModel>) jdbcTemplate.query(getQuery,new Object[]{id, id, since, limit}, new PostRowMapper());
        }

        String getQuery = "SELECT author, created, forum, id, isedited, message, parent, thread FROM posts WHERE thread = ? ";
        if(desc){
            getQuery += " and id < ? order by id  DESC ";
        }else{
            getQuery += " and id > ? order by id  ";
        }
        getQuery += " LIMIT ? ";
        System.out.println(getQuery);
        return (ArrayList<PostModel>) jdbcTemplate.query(getQuery,new Object[]{id, since, limit}, new PostRowMapper());

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
        if ( post.getMessage() != null && !post.getMessage().equals( oldPost.getMessage())){
            oldPost.setMessage(post.getMessage());
            oldPost.setIsEdited(true);
        }

        jdbcTemplate.update(
                createQuery,
                oldPost.getAuthor(),
                oldPost.getCreated(),
                oldPost.getForum(),
                oldPost.getIsEdited(),
                oldPost.getMessage(),
                oldPost.getId()
        );
        return oldPost;
    }

    public PostFullModel getFullModel(int id,
                                      String related){

        PostFullModel answer = new PostFullModel(getPostById(id));
        if(answer.getPost() == null){
            throw new RuntimeException();
        }
        if(!related.equals("")){
            if(related.contains("user")){
                answer.setAuthor(userService.getUserByNickname(answer.getPost().getAuthor()));
            }
            if (related.contains("forum")){
                answer.setForum(forumService.getForumBySlug(answer.getPost().getForum()));
            }
            if(related.contains("thread")){
                answer.setThread(threadService.getThreadBySlugOrId(
                        String.valueOf(answer.getPost().getThread())
                ));
            }
        }
        return answer;

    }

    public static class PostRowMapper implements RowMapper<PostModel> {
        @Override
        public PostModel mapRow(ResultSet resSet, int rowNum) throws SQLException {
            Timestamp timestamp = resSet.getTimestamp("created");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return new PostModel(
                    resSet.getString("author"),
                    dateFormat.format(timestamp.getTime()),
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
