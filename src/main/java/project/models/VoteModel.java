package project.models;

public class VoteModel {

    private String nickname;
    private int thread;
    private int voice;

    public VoteModel() {
    }

    public VoteModel(String nickname, int thread, int voice) {
        this.nickname = nickname;
        this.thread = thread;
        this.voice = voice;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

    public int getVoice() {
        return voice;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }
}