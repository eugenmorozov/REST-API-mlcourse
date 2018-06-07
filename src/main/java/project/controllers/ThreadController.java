package project.controllers;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import project.models.*;
import project.services.ForumService;
import project.services.PostService;
import project.services.ThreadService;
import project.services.UserService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("api/thread")
public class ThreadController {
    private PostService postService;
    private ThreadService threadService;
    private UserService userService;

    public ThreadController(PostService postService, ThreadService threadService, UserService userService) {
        this.postService = postService;
        this.threadService = threadService;
        this.userService = userService;
    }

    @GetMapping(path = "/{slug_or_id}/details")
    public ResponseEntity slugDetails(
            @PathVariable("slug_or_id") String slugOrId
    ) {
        ThreadModel thread = threadService.getThreadBySlugOrId(slugOrId);
        if (thread != null) {
            return ResponseEntity.status(HttpStatus.OK).body(thread);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("thread not found"));
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("thread not found"));
        }
    }

    @PostMapping(path = "/{slug_or_id}/vote")
    public ResponseEntity voteThread(
            @PathVariable("slug_or_id") String slugOrId,
            @RequestBody VoteModel vote
    ) {
            ThreadModel thread = threadService.getThreadBySlugOrId(slugOrId);
            if(thread != null) {
                vote.setThread(thread.getId());
                try{

                    return ResponseEntity.status(HttpStatus.OK).body(threadService.setVote(vote));
                }catch(RuntimeException err){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("threads not found"));
                }
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("thread not found"));
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
                posts = postService.CreatePosts(posts, thread);
                if(posts == null){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("user doesnt exists"));
                }else {
                    return ResponseEntity.status(HttpStatus.CREATED).body(posts);
                }
            }catch (RuntimeException error){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorModel("no parent post"));
            }
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("thread not found"));
        }
    }

    @GetMapping(path = "/{slugOrId}/posts")
    public ResponseEntity getUsers(
            @PathVariable("slugOrId") String slugOrId,
            @RequestParam(value = "limit", required = false, defaultValue = "9999999") Integer limit,
            @RequestParam(value = "since", required = false) Integer since,
            @RequestParam(value = "sort", required = false, defaultValue = "flat") String sort,
            @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc
    ){
        ThreadModel thread = threadService.getThreadBySlugOrId(slugOrId);
        if(since == null){
            if(desc) {
                since = 999999;
            }else{
                since = 0;
            }
        }
        if(thread != null) {
            int id = thread.getId();
            return ResponseEntity.status(HttpStatus.OK).body(postService.getPosts(id,limit,since,sort,desc));
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("there's no such thread"));
        }

    }
}
