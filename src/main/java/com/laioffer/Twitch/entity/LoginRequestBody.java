package com.laioffer.Twitch.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequestBody {
    private final String userId;
    private final String password;

    @JsonCreator
    // jackson use this constructor to transfer the json string to object
    // JsonCreator和JsonProperty配合出现
    public LoginRequestBody(@JsonProperty("user_id") String userId, @JsonProperty("password") String password) {
        this.userId = userId;
        this.password = password;
    }


    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }
}
