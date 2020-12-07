package com.laioffer.Twitch.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.Twitch.entity.Game;
import com.laioffer.Twitch.external.TwitchClient;
import com.laioffer.Twitch.external.TwitchException;
import org.json.JSONObject;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GameServlet", urlPatterns = {"/game"})
public class GameServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // stream -> String -> json object (hashmap)
        JSONObject jsonRequest = new JSONObject(IOUtils.toString(request.getReader()));
        String name = jsonRequest.getString("name");
        String developer = jsonRequest.getString("developer");
        String releaseTime = jsonRequest.getString("release_time");
        String website = jsonRequest.getString("website");
        float price = jsonRequest.getFloat("price");

        System.out.println("Name is: " + name);
        System.out.println("Developer is: " + developer);
        System.out.println("Release time is: " + releaseTime);
        System.out.println("Website is: " + website);
        System.out.println("Price is: " + price);

        response.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "ok");
        response.getWriter().print(jsonResponse);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        String gamename = request.getParameter("gamename");

//        JSONObject object = new JSONObject();
//        object.put("name", gamename);  // 产生json结构的String {}, :
//        object.put("price", 24);
//        response.setContentType("application/json");
//        response.getWriter().print(object); // object.toString() 应该重写了
//        // String objectString = "{name:\"xue\"}";

//        Game game = new Game("xueyang", "xueyang","xueyang","xueyang", 10);
//
//        // object -> string JsonObject没用了
//        ObjectMapper mapper = new ObjectMapper();
//
//        response.getWriter().print(mapper.writeValueAsString(game));
        String gameName = request.getParameter("game_name");
        TwitchClient client = new TwitchClient();

        response.setContentType("application/json;charset=UTF-8");
        try {
            if (gameName != null) {
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.searchGame(gameName)));
            } else {
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.topGames(0)));
            }
        } catch (TwitchException e) {
            throw new ServletException(e);
        }


    }
}
