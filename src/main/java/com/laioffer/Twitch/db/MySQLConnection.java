package com.laioffer.Twitch.db;

import com.laioffer.Twitch.entity.Game;
import com.laioffer.Twitch.entity.Item;
import com.laioffer.Twitch.entity.ItemType;

import java.sql.*;
import java.util.*;

public class MySQLConnection {
    private final Connection conn;

    public MySQLConnection() throws MySQLException {
        try {
            // Driver driver = new Driver() // in compile time must has the class
            // 13 line, when compile we can not have the class, ignore in compile time
            // reflection -> when in runtime, we can make sure the behaviour
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(MySQLDBUtil.getMySQLAddress());
        } catch (Exception e) {
            e.printStackTrace();
            throw new MySQLException("Failed to connect to Database");
        }
    }

    // why close the conn?
    // 1, overlap the resource max link connected to the database
    // 2, conn will use the local memory, will fasten the local computation
    //     connect to data -> read in memory -> return to front
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setFavoriteItem(String userId, Item item) throws MySQLException{
        if(conn == null) {
            throw new MySQLException("failed to connect to database");
        }

        // TODO: insert userId and itemId in favorite table, foreign key must exist
        saveItem(item);
        // maybe insert item to the item table if not exists
        //"INSERT IGONORE INTO": if we insert the item that has already been in the database, will throw exception

        // ???????????how to generate the sql sentence ????
//        String template = "INSERT INTO favorite_records (user_id, item_id) VALUES (%s, %s)";
//        String sql = String.format(template, userId, item.getId());

        // JDBC provide the question mark filling, the ? is a common string

        String sql = "INSERT IGONORE INTO favorite_records (user_id, item_id) VALUES (?, ?)";
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, item.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new MySQLException("failed to insetr the date ");
        }

    }

    // if other's favorite this item
    // if only you, after you unset, you should delete that records
    // TODO !!! or you can keep it and after a fixed time, delete once, response fast !!!
    public void unsetFavoriteItem(String userId, String itemId) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        String sql = "DELETE FROM favorite_records WHERE user_id = ? AND item_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to delete favorite item to Database");
        }
    }

    public void saveItem(Item item) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, item.getId());
            statement.setString(2, item.getTitle());
            statement.setString(3, item.getUrl());
            statement.setString(4, item.getThumbnailUrl());
            statement.setString(5, item.getBroadcasterName());
            statement.setString(6, item.getGameId());
            statement.setString(7, item.getType().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to add item to Database");
        }
    }

    public Map<String, List<Item>> getFavoriteItems(String userId) throws MySQLException{
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }

        Set<String> favoriteItemIds = getFavoriteItemIds(userId);

        String sql = "SELECT * FROM items WHERE id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {

                    // string -> enum格式 -> constructor
                    ItemType itemType = ItemType.valueOf(rs.getString("type"));
                    Item item = new Item.Builder().id(rs.getString("id")).title(rs.getString("title"))
                            .url(rs.getString("url")).thumbnailUrl(rs.getString("thumbnail_url"))
                            .broadcasterName(rs.getString("broadcaster_name")).gameId(rs.getString("game_id")).type(itemType).build();
                    itemMap.get(rs.getString("type")).add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get favorite items from Database");
        }
        return itemMap;

    }



    // the following two is used in recommendation
    public Set<String> getFavoriteItemIds(String userId) throws MySQLException{
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        String sql = "SELECT item_id FROM favorite_records WHERE user_id = ?";
        Set<String> favoriteItemIds = new HashSet<>();
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            // image it is a table returned by query, rs point to [-1, null] iterator use next to read
            ResultSet rs = statement.executeQuery();
            // java use int means true?
            // rs jump to next line and return if next line exist; start from -1 (hasNext, next)
            while(rs.next()) {
                String itemId = rs.getString("item_id");
                favoriteItemIds.add(itemId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to delete favorite item to Database");
        }
        return favoriteItemIds;
    }

    public Map<String, List<String>> getFavoriteGameIds(Set<String> favoriteItemIds) throws MySQLException {
        if (conn == null) {
            System.err.println("DB connection failed");
            throw new MySQLException("Failed to connect to Database");
        }
        Map<String, List<String>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }
        String sql = "SELECT game_id, type FROM items WHERE id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    itemMap.get(rs.getString("type")).add(rs.getString("game_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLException("Failed to get favorite game ids from Database");
        }
        return itemMap;
    }



}


