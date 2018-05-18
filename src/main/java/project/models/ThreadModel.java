package project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class ThreadModel {
    private String author;
    private String created;
    private String forum;
    private int id;
    private String message;
    private String slug;
    private String title;
    private int votes;

    public ThreadModel(){
    }
    @JsonCreator
    public ThreadModel(@JsonProperty(value = "author") String author, //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                       @JsonProperty(value = "created") String created,
                       @JsonProperty(value = "forum") String forum,
                       @JsonProperty(value = "id") int id,
                       @JsonProperty(value = "message") String message,
                       @JsonProperty(value = "slug") String slug,
                       @JsonProperty(value = "title") String title,
                       @JsonProperty(value = "votes") int votes) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.message = message;
        this.slug = slug;
        this.title = title;
        this.votes = votes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

}
