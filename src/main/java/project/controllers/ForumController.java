package project.controllers;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.SerializationUtils;
import org.springframework.web.bind.annotation.*;

import project.models.ErrorModel;
import project.models.ForumModel;
import project.models.ThreadModel;
import project.models.UserModel;
import project.services.ForumService;
import project.services.ThreadService;
import project.services.UserService;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Controller
@RequestMapping("api/forum")
public class ForumController {

    private UserService userService;
    private ForumService forumService;
    private ThreadService threadService;

    private ForumController(UserService userService, ForumService forumService, ThreadService threadService){
        this.forumService = forumService;
        this.userService = userService;
        this.threadService = threadService;
    }

    @PostMapping(path = "/create")
    public ResponseEntity create(
            @RequestBody ForumModel forum) {
        UserModel user =  userService.getUserByNickname( forum.getUser() );
        if (user  == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("no such user"));
        } else {
            try {
                forum.setPosts(0);
                forum.setThreads(0);
                forum.setUser(user.getNickname());
                forumService.createForum(forum);
                return ResponseEntity.status(HttpStatus.CREATED).body(
                    forumService.getForumBySlug(forum.getSlug())
                );
            } catch (Exception error) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body( forumService.getForumBySlug( forum.getSlug() ) );
            }
        }
    }

    @PostMapping(path = "/{slug}/create")
    public  ResponseEntity createThread(
            @PathVariable("slug") String slug,
            @RequestBody ThreadModel thread
    ) {
        ForumModel forum = forumService.getForumBySlug( slug );
        if( userService.getUserByNickname( thread.getAuthor() ) != null &&  forum != null) {
            try {
                thread.setVotes(0);
                thread.setForum(forum.getSlug());
                thread.setId(threadService.generateId());
                if(thread.getCreated() == null){
                    String currentTime = ZonedDateTime.now().
                            format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
                    thread.setCreated(currentTime);
                }
                System.out.println("created thread: "+slug);
                return ResponseEntity.status(HttpStatus.CREATED).body(threadService.createThread(thread));
            } catch (DataAccessException error) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body( threadService.getThreadBySlug( thread.getSlug() ) );
            }
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("user or forum doesn't exist"));
        }
    }

    @GetMapping(path = "/{slug}/details")
    public ResponseEntity getDetails(
            @PathVariable("slug") String slug
    ){
        ForumModel forum = forumService.getForumBySlug( slug );
        if(forum != null) {
            return ResponseEntity.status(HttpStatus.OK).body(forum);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("not found"));
        }

    }
    @GetMapping(path = "/{slug}/threads")
    public ResponseEntity getThreads(
            @PathVariable("slug") String slug,
            @RequestParam(value = "limit", required = false, defaultValue = "9999") Integer limit,
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc
    ){
        //efaultValue = "1000-01-01 00:00:00.000"
        if(since == null){
            if(desc) {
                since = " 2518-07-06T19:44:44.813+03:00";
            }else{
                since = " 1018-07-06T19:44:44.813+03:00";
            }
        }

        if (forumService.getForumBySlug(slug) != null){
            return ResponseEntity.status(HttpStatus.OK).body(threadService.getThreadsBySlug(slug,limit,since,desc));
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("forum not found"));
        }
    }

    @GetMapping(path = "/{slug}/users")
    public ResponseEntity getUsers(
            @PathVariable("slug") String slug,
            @RequestParam(value = "limit", required = false, defaultValue = "9999") Integer limit,
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc
    ){
        if(since == null){
            if(desc){
                since = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
            }else{
                since = "";
            }
        }
        System.out.println("FUCK THERES USERS");
        ForumModel forum = forumService.getForumBySlug(slug);
        if(forum != null) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(userService.getUsersByThreadAndPost(slug, limit, since, desc));
            } catch (Exception error) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("there's no such forum"));
            }
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("there's no such forum"));
        }
    }

}
