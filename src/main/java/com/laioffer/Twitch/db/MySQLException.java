package com.laioffer.Twitch.db;


// 除了名字和父类不一样其他都一样
public class MySQLException extends RuntimeException {
    public MySQLException(String errorMessage) {
        super(errorMessage);
    }
}

