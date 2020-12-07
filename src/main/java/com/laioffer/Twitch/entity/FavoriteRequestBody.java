package com.laioffer.Twitch.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FavoriteRequestBody {

    // @JsonProperty("favorite") if we put here, we can reverse object to json string
    private final Item favoriteItem;

    // 类似于jsondeserilization when you have builder pattern
    // change json string to object
    @JsonCreator
    public FavoriteRequestBody(@JsonProperty("favorite") Item favoriteItem) {
        this.favoriteItem = favoriteItem;
    }

    public Item getFavoriteItem() {
        return favoriteItem;
    }

}
