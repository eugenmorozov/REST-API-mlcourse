package project.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.models.*;
import project.services.PostService;

@Controller
@RequestMapping("api/post")
public class PostController {
    private PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(path = "/{id}/details")
    public ResponseEntity updatePost(
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

    @GetMapping(path = "/{id}/details")
    public ResponseEntity getPostDetails(
            @PathVariable("id") int id,
            @RequestParam(value = "related", required = false, defaultValue = "") String related
    ){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(postService.getFullPost(id, related));
        }catch (Exception err){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel("cant find post"));
        }
    }
}
