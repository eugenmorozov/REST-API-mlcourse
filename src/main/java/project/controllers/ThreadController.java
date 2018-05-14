package project.controllers;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import project.models.ForumModel;
import project.models.PostModel;
import project.models.ThreadModel;
import project.models.UserModel;
import project.services.ForumService;
import project.services.PostService;
import project.services.ThreadService;
import project.services.UserService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/thread")
public class ThreadController {
    private PostService postService;
    private ThreadService threadService;

    public ThreadController(PostService postService, ThreadService threadService) {
        this.postService = postService;
        this.threadService = threadService;
    }

    @GetMapping(path = "/{slug_or_id}/details")
    public ResponseEntity slugDetails(
            @PathVariable("slug_or_id") String slugOrId
    ) {
        ThreadModel thread = threadService.getThreadBySlugOrId(slugOrId);
        if (thread != null) {
            return ResponseEntity.status(HttpStatus.OK).body(thread);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("thread not found");
        }
    }

    @PostMapping(path = "/{slug_or_id}/details")
    public ResponseEntity updateThread(
            @PathVariable("slug_or_id") String slugOrId,
            @RequestBody ThreadModel thread
    ) {
        ThreadModel oldThread = threadService.getThreadBySlugOrId(slugOrId);
        if (oldThread != null) {
            return ResponseEntity.status(HttpStatus.OK).body(threadService.updateThread(thread,oldThread));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("thread not found");
        }
    }

    @PostMapping(path = "/{slug_or_id}/create")
    public  ResponseEntity createPosts(
            @PathVariable("slug_or_id") String slugOrId,
            @RequestBody List<PostModel> posts
    ) {
        ThreadModel thread =  threadService.getThreadBySlugOrId(slugOrId);
        if( thread != null){
            try {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(postService.CreatePosts(posts, thread));
            }catch (RuntimeException error){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("no parent post");
            }
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("thread not found");
        }
    }
}
