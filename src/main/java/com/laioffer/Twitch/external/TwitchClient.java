package com.laioffer.Twitch.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.Twitch.entity.Game;
import com.laioffer.Twitch.entity.Item;
import com.laioffer.Twitch.entity.ItemType;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


import java.io.IOException;
import java.util.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TwitchClient {
    private static final String TOKEN = "Bearer by6f1lhxaxa7pzx9i69xtya8nr8c9o";
    private static final String CLIENT_ID = "s3yzzitncfcbi9ucwtt8dx52q2yoce";
    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;
    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    private static final int DEFAULT_SEARCH_LIMIT = 20;


    private String buildGameURL(String url, String gameName, int limit) {
        if (gameName.equals("")) {
            return String.format(url, limit);
        } else {
            try {
                // among us -> among%20us
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format(url, gameName);
        }
    }

    private String buildSearchURL(String url, String gameId, int limit) {
        try{
            gameId = URLEncoder.encode(gameId, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format(url, gameId, limit);
    }


    private String searchTwitch(String url) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // lamda function
        ResponseHandler<String> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if(status != 200) {
                throw new TwitchException("Failed to connnect to the Twitch");
            }
            HttpEntity entity = response.getEntity();
            if(entity == null) {
                throw new TwitchException("get no data");
            }
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            return obj.getJSONArray("data").toString();
        };

        HttpGet httpget = new HttpGet(url);

        //header 里面的东西不会体现在url上面
        httpget.setHeader("Authorization", TOKEN);
        httpget.setHeader("Client-Id", CLIENT_ID);

        try {
            return httpclient.execute(httpget, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TwitchException("failed");
        }finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                // 链接断开出错不影响前端，中间商自己消化，但是我确实不知道怎么处理，哈哈哈哈哈所以framework觉得我太蠢了
                e.printStackTrace();
            }
        }
    }

    private List<Game> getGameList(String data) throws TwitchException {

        // json格式的String对应成object, 具体怎么对应体现在annotation里面
        // 如果没有一一对应，没有完全convert，可能有会exception，要加annotation

        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Game[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API");
        }
    }


    public List<Game> topGames(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        String url = buildGameURL(TOP_GAME_URL, "", DEFAULT_GAME_LIMIT);
        String response = searchTwitch(url);
        return getGameList(response);
    }

    public Game searchGame(String gameName) throws TwitchException {
        List<Game> gameList = getGameList(searchTwitch(buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0)));
        if (gameList.size() != 0) {
            return gameList.get(0);
        }
        return null;
    }

    private List<Item> getItemList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Item[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse item data from Twitch API");
        }
    }

    private List<Item> searchStreams(String gameId, int limit) throws TwitchException {
        List<Item> streams = getItemList(searchTwitch(buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : streams) {
            item.setType(ItemType.STREAM);
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
        }
        return streams;
    }
    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        List<Item> clips = getItemList(searchTwitch(buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : clips) {
            item.setType(ItemType.CLIP);
        }
        return clips;
    }
    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        List<Item> videos = getItemList(searchTwitch(buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : videos) {
            item.setType(ItemType.VIDEO);
        }
        return videos;
    }

    public List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = Collections.emptyList();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break;
            case VIDEO:
                items = searchVideos(gameId, limit);
                break;
            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }

        // Update gameId for all items. GameId is used by recommendation function
        for (Item item : items) {
            item.setGameId(gameId);
        }
        return items;
    }

    public Map<String, List<Item>> searchItems(String gameId) throws TwitchException {
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), searchByType(gameId, type, DEFAULT_SEARCH_LIMIT));
        }
        return itemMap;
    }



}
