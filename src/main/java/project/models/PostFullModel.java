package project.models;

public class PostFullModel {
    private UserModel author;
    private ForumModel forumModel;
    private PostModel post;
    private ThreadModel thread;

    public PostFullModel(
            UserModel author,
            ForumModel forumModel,
            PostModel post,
            ThreadModel thread) {
        this.author = author;
        this.forumModel = forumModel;
        this.post = post;
        this.thread = thread;
    }

    public UserModel getAuthor() {
        return author;
    }

    public void setAuthor(UserModel author) {
        this.author = author;
    }

    public ForumModel getForumModel() {
        return forumModel;
    }

    public void setForumModel(ForumModel forumModel) {
        this.forumModel = forumModel;
    }

    public PostModel getPost() {
        return post;
    }

    public void setPost(PostModel post) {
        this.post = post;
    }

    public ThreadModel getThread() {
        return thread;
    }

    public void setThread(ThreadModel thread) {
        this.thread = thread;
    }
}
