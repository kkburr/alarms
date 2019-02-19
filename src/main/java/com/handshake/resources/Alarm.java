package com.handshake.resources;

public class Alarm {
    private Integer id;
    private String text;

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    private Integer votes;

    public Alarm(Integer id, Integer votes, String text) {
        setVotes(votes);
        setId(id);
        setText(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
