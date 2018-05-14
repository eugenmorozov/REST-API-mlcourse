package project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;

import java.io.Console;
import java.util.List;

import project.models.UserModel;
import project.services.UserService;

@Controller
@RequestMapping("/user")
public class UserController{

    private UserService userService;

    private UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping(path = "/{nickname}/create")
    public ResponseEntity create(
            @PathVariable("nickname") String nickname,
            @RequestBody UserModel user) {
        try {
            user.setNickname(nickname);
            //System.out.println(user.getAbout()+user.getFullname()+user.getEmail()+user.getNickname());
            userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }catch (DataAccessException error){
            return  ResponseEntity.status(HttpStatus.CONFLICT).body(userService.getSameUsers(nickname,user.getEmail()));
        }
    }

    @GetMapping(path = "/{nickname}/profile")
    public ResponseEntity getProfile(
            @PathVariable("nickname") String nickname
    ){
        UserModel user = userService.getUserByNickname(nickname);
        if(user!=null){
            return ResponseEntity.status(HttpStatus.OK).body(user);
        }else{
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't find user by nickname: "+ nickname);
        }

    }

    @PostMapping(path = "/{nickname}/profile")
    public ResponseEntity updateUser(
            @PathVariable("nickname") String nickname,
            @RequestBody UserModel user
    ){
        user.setNickname(nickname);
        UserModel oldUser = userService.getUserByNickname(nickname);
        if ( oldUser == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no such user");
        }else{
            try{
                 return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserByNickname(user, oldUser) );
            }catch (DataAccessException error){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("conflict with another user");
            }
        }
    }
}