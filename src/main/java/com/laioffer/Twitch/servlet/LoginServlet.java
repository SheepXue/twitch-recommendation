package com.laioffer.Twitch.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.Twitch.db.MySQLConnection;
import com.laioffer.Twitch.db.MySQLException;
import com.laioffer.Twitch.entity.LoginRequestBody;
import com.laioffer.Twitch.entity.LoginResponseBody;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        LoginRequestBody body = mapper.readValue(request.getReader(), LoginRequestBody.class);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        MySQLConnection connection = null;
        String userName = "";
        try {
            connection = new MySQLConnection();
            String userId = body.getUserId();
            String password = ServletUtil.encryptPassword(userId, body.getPassword());
            userName = connection.verifyLogin(userId, password);
        } catch (MySQLException e) {
            throw new ServletException(e);
        } finally {
            connection.close();
        }

        if(!userName.isEmpty()) {
            // the session id will generate automatically
            HttpSession session = request.getSession();

            session.setAttribute("user_id", body.getUserId());
            session.setAttribute("userName", userName);
            session.setMaxInactiveInterval(600);

            
            // session id放在 response header里面返回是tomcat帮助你完成的
            LoginResponseBody responseBody = new LoginResponseBody(body.getUserId(),userName);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(new ObjectMapper().writeValueAsString(responseBody));

        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }


    }


}
