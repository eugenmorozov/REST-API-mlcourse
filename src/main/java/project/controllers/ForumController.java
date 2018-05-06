package project.controllers;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import project.models.ForumModel;
import project.models.ThreadModel;
import project.models.UserModel;
import project.services.ForumService;
import project.services.ThreadService;
import project.services.UserService;

import java.util.ArrayList;

@Controller
@RequestMapping("/forum")
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
        if ( userService.getUserByNickname( forum.getUser() ) == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such user");
        } else {
            try {
                forum.setPosts(0);
                forum.setThreads(0);
                forumService.createForum(forum);
                return ResponseEntity.status(HttpStatus.CREATED).body(forum);
            } catch (DataAccessException error) {
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
        if( userService.getUserByNickname( thread.getAuthor() ) != null && forumService.getForumBySlug( slug ) != null) {
            try {
                thread.setVotes(0);
                thread.setForum(slug);
                System.out.println(thread.getSlug()+thread.getForum()+thread.getCreated().toString());
                threadService.createThread(thread);
                return ResponseEntity.status(HttpStatus.CREATED).body(thread);
            } catch (DataAccessException error) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body( threadService.getThreadBySlug( thread.getSlug() ) );

            }
        }
        else {return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user or forum doesnt exist");}
    }

    @GetMapping(path = "/{slug}/details")
    public ResponseEntity getDetails(
            @PathVariable("slug") String slug
    ){
        ForumModel forum = forumService.getForumBySlug( slug );
        if(forum != null) {
            return ResponseEntity.status(HttpStatus.OK).body(forum);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found");
        }

    }
    @GetMapping(path = "/{slug}/threads")
    public ResponseEntity getThreads(
            @PathVariable("slug") String slug
    ){
        ArrayList<ThreadModel> threads  = threadService.getThreadsBySlug(slug);
        if(threads != null){
            return ResponseEntity.status(HttpStatus.OK).body(threads);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("there's no such forum");
        }
    }

    @GetMapping(path = "/{slug}/users")
    public ResponseEntity getUsers(
            @PathVariable("slug") String slug
    ){
        //TODO: write method
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(" ");
    }

}
