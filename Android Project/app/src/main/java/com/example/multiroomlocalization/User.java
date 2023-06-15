package com.example.multiroomlocalization;

public class User {
    private final String username;
    private final String password;

    User(String username,String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
