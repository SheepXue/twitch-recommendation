package com.laioffer.Twitch.external;

public class TwitchException extends RuntimeException{
    public TwitchException(String errorMessage) {
        super(errorMessage);
    }
}
