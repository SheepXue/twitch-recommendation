package com.laioffer.Twitch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponseBody {
    // jackson transfer the java object to string do not need json creator
    @JsonProperty("user_id")
    private final String userId;

    @JsonProperty("name")
    private final String name;

    public LoginResponseBody(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

}
