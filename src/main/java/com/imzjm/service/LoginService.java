package com.imzjm.service;

import com.imzjm.pojo.Student;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LoginService {

    public String getToken() {//在html中抠出Token
        HttpClient httpClient = HttpClient.newHttpClient();

        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()          //http://wgxy.cdvisor.com/login.xhtml
                    .uri(new URI("https://www.cdvisor.com:8443/4142012738/login.xhtml"))
                    .build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        Matcher matcher = Pattern.compile("(?<=tokenstr\" type=\"hidden\" value=\")([^\"]*)").matcher(response.body());
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    public String getCookie(Student iKun /*,String token,*/) {//发送登录请求，获取cookie

        //官方网页扣下来的(值不变)，登录时要携带，作用不详
        String aStr = "04B5991DBC53A67DD0DFADDC87225930";

        HttpClient httpClient = HttpClient.newHttpClient();
        //请求参数
        StringBuilder params = new StringBuilder();

        //params.append("tokenstr=").append(token);
        params.append("astr=").append(aStr);

        params.append("&");
        params.append("action=login&");
        params.append("auser=").append(iKun.getUser());
        params.append("&");
        params.append("apass=").append(iKun.getPass());
        params.append("&");
        params.append("acode=whgc");

        HttpResponse<String> response;
        try {                                                       //http://wgxy.cdvisor.com/login.xhtml
            HttpRequest request = HttpRequest.newBuilder(new URI("https://www.cdvisor.com:8443/4142012738/loginajax1.xhtml"))
                    .POST(HttpRequest.BodyPublishers.ofString(params.toString()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!response.headers().allValues("Set-Cookie").isEmpty()) {
            String cookie = response.headers().allValues("Set-Cookie").get(0);
            Matcher matcher = Pattern.compile("(?<=userinfo=)([^;]*)").matcher(cookie);
            if (matcher.find())
                return matcher.group();
        }
        return "";

    }
}
