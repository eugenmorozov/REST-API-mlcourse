package project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserModel {
    private String about;
    private String email;
    private String fullname;
    private String nickname;

    public UserModel(){

    }
    public UserModel(UserModel user){
        this.about = user.about;
        this.email = user.email;
        this.fullname = user.fullname;
        this.nickname = user.nickname;
    }

    @JsonCreator
    public UserModel(
            @JsonProperty("about") String about,
            @JsonProperty("email") String email,
            @JsonProperty("fullname") String fullname,
            @JsonProperty("nickname") String nickname
    ) {
        this.about = about;
        this.email = email;
        this.fullname = fullname;
        this.nickname = nickname;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
