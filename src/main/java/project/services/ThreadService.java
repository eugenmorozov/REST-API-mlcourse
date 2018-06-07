package project.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import project.models.ThreadModel;
import project.models.UserModel;
import project.models.VoteModel;

@Repository
public class ThreadService {
    private JdbcTemplate jdbcTemplate;
    private UserService userService;

    public ThreadService(JdbcTemplate jdbcTemplate, UserService userService) {
        this.jdbcTemplate = jdbcTemplate;
        this.userService = userService;
    }


    @Transactional
    public ThreadModel createThread(ThreadModel thread) {
        jdbcTemplate.update(
                "UPDATE forums SET threads = threads + 1 WHERE slug = ?::citext",
                thread.getForum()
        );
        UserModel user = userService.getUserByNickname(thread.getAuthor());
        jdbcTemplate.update(
                "INSERT INTO forum_users (about, fullname,nickname, email, forum) VALUES (?,?,?,?,?)",
                user.getAbout(),
                user.getFullname(),
                user.getNickname(),
                user.getEmail(),
                thread.getForum()
        );
        //thread.setId(generateId());
        //System.out.println("OH SHIT                  "+String.valueOf(thread.getId()));
        //int userId = userService.getUserIdByNickname(thread.getAuthor());
        if(thread.getSlug() == null ){
            return jdbcTemplate.queryForObject(
                    "INSERT INTO threads (author, created, forum, message, title, votes) VALUES ( ?::citext, ?::TIMESTAMPTZ, ?, ?, ?, ? ) RETURNING *" ,
                    new ThreadRowMapper(),
                    thread.getAuthor(),
                    thread.getCreated(),
                    thread.getForum(),
                    thread.getMessage(),
                    thread.getTitle(),
                    thread.getVotes()
            );
        }
        return  jdbcTemplate.queryForObject(
                "INSERT INTO threads (author, created, forum, message, slug, title, votes) VALUES (?, ?::TIMESTAMPTZ, ?, ?, ?, ?, ?) RETURNING *" ,
                new ThreadRowMapper(),
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
            String since,
            boolean desc
    ){
        String query ="SELECT * FROM threads WHERE forum = ?::citext ";
        if(since == null){
            if (desc) {
                query += "  ORDER BY created DESC ";
            } else {
                query += "  ORDER BY created ";
            }
            query += " LIMIT ? ";
            return (ArrayList<ThreadModel>) jdbcTemplate.query(
                    query,
                    new Object[]{slug, limit},
                    new ThreadRowMapper()
            );

        }else {
            if (desc) {
                query += "  AND created <= ?::timestamptz ORDER BY created DESC ";
            } else {
                query += "  AND created >= ?::timestamptz ORDER BY created ";
            }
            query += " LIMIT ? ";
            return (ArrayList<ThreadModel>) jdbcTemplate.query(
                    query,
                    new Object[]{slug, since,limit},
                    new ThreadRowMapper()
            );
        }

        //System.out.println(query);


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


    @Transactional
    public ThreadModel setVote(VoteModel vote){
        if (getVote(vote) != null) {
            jdbcTemplate.update(
                    "UPDATE votes SET (nickname, thread, voice) = (?, ?, ?) WHERE nickname = ?::citext AND thread = ?",
                    vote.getNickname(),
                    vote.getThread(),
                    vote.getVoice(),
                    vote.getNickname(),
                    vote.getThread()
            );
        } else {
            System.out.println(String.valueOf(vote.getThread()));
            jdbcTemplate.update(
                    "INSERT INTO  votes (nickname, thread, voice) VALUES (?, ?, ?)",
                    vote.getNickname(),
                    vote.getThread(),
                    vote.getVoice()
            );
        }
        updateVotesById(vote.getThread());
        return getThreadBySlugOrId(String.valueOf(vote.getThread()));
    }
    public void updateVotesById (int thread){
        int rating = jdbcTemplate.queryForObject(
                "SELECT sum(voice) FROM votes WHERE thread = ?",
                new Object[]{thread},
                int.class);
        jdbcTemplate.update(
                "UPDATE threads SET votes = ? WHERE id = ?",
                rating,
                thread
        );
    }
    public ThreadModel updateThread( ThreadModel thread, ThreadModel oldThread){
        String createQuery = "UPDATE threads SET ( author, created, forum, message, slug, title) = (?,?::timestamptz,?,?,?,?) WHERE slug = ?::citext";
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

    public VoteModel getVote ( VoteModel vote){
        try{
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM votes WHERE nickname = ?::citext AND  thread = ?",
                    new Object[]{ vote.getNickname(), vote.getThread()},
                    new ThreadService.VoteRowMapper()
            );
        }catch (DataAccessException error){
            return null;
        }
    }


    public static class VoteRowMapper implements RowMapper<VoteModel>{
        @Override
        public VoteModel mapRow(ResultSet resultSet, int rowNum) throws SQLException{
            return new VoteModel(
                    resultSet.getString("nickname"),
                    resultSet.getInt("thread"),
                    resultSet.getInt("voice")
            );
        }
    }

    public static class ThreadRowMapper implements RowMapper<ThreadModel> {
        @Override
        @Nullable
        public ThreadModel mapRow(ResultSet resSet, int rowNum) throws SQLException {
            Timestamp timestamp = resSet.getTimestamp("created");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return new ThreadModel(
                    resSet.getString("author"),
                    dateFormat.format(timestamp.getTime()),
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
