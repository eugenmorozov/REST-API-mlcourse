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
@RequestMapping("api/post")
public class PostController {
    private PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(path = "/{id}/details")
    public ResponseEntity getDetails(
            @PathVariable("id") int id,
            @RequestBody PostModel post
   ){
        PostModel oldPost = postService.getPostById(id);
        if(oldPost != null){
            return ResponseEntity.status(HttpStatus.OK).body(postService.updatePost(post, oldPost));
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("post doesn't exists"));
        }
    }
}
