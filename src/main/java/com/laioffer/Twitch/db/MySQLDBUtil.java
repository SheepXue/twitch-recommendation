package com.laioffer.Twitch.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.Properties;

// 连接我的数据库，第一步获取数据库的地址
public class MySQLDBUtil {
    private static final String INSTANCE = "database-twitch.c0lwnfcn8omn.us-east-2.rds.amazonaws.com";
    private static final String PORT_NUM = "3306";
    public static final String DB_NAME = "twitch";
    public static String getMySQLAddress() throws IOException {

        // property 本质是hashtable
        Properties prop = new Properties();
        String propFileName = "config.properties";

        // 数据是一段一段的，可能文件太大
        InputStream inputStream = MySQLDBUtil.class.getClassLoader().getResourceAsStream(propFileName);
        prop.load(inputStream);

        String username = prop.getProperty("user");
        String password = prop.getProperty("password");

        // java database connector, 注意连接的名字
        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true&serverTimezone=UTC&createDatabaseIfNotExist=true",
                INSTANCE, PORT_NUM, DB_NAME, username, password);

    }

}
