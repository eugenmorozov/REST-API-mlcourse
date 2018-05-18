package project.models;

public class PostFullModel {
    private UserModel author;
    private ForumModel forum;
    private PostModel post;
    private ThreadModel thread;

    public PostFullModel(
            UserModel author,
            ForumModel forumModel,
            PostModel post,
            ThreadModel thread) {
        this.author = author;
        this.forum = forumModel;
        this.post = post;
        this.thread = thread;
    }

    public PostFullModel(PostModel post) {
        this.post = post;
    }

    public UserModel getAuthor() {
        return author;
    }

    public void setAuthor(UserModel author) {
        this.author = author;
    }

    public ForumModel getForum() {
        return forum;
    }

    public void setForum(ForumModel forum) {
        this.forum = forum;
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
