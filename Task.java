package com.codurance.training.tasks;

public final class Tasks {
    public final long id;
    public final String description;
    public boolean done;

    public Tasks(long id, String description, boolean done) {
        this.id = id;
        this.description = description;
        this.done = done;
    }
}