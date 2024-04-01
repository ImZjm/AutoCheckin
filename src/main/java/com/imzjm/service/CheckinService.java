package com.imzjm.service;

import com.imzjm.pojo.Student;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CheckinService {
    @Autowired
    LoginService loginService;
    @Autowired
    StudentService studentService;

    public void updateCheckInfo(Student student){
        HttpClient httpClient = HttpClient.newHttpClient();

        //垃圾网站,发送请求时,把wd和jd搞个反的,这里只能将错就错
        String body = "wd=" + student.getLongitude() + "&" + "jd=" + student.getLatitude();
        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI("https://www.cdvisor.com:8443/4142012738/app/sxjy/s_sx_sxqd.xhtml?action=qdget"))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.47")
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        String data = response.body();

        //更新省
        Matcher sheng = Pattern.compile("(?<=\"province\":\")([^\"]*)").matcher(data);
        if (sheng.find())
            student.setSheng(sheng.group());

        //更新市
        Matcher shi = Pattern.compile("(?<=\"city\":\")([^\"]*)").matcher(data);
        if (shi.find())
            student.setShi(shi.group());

        //更新区
        Matcher qu = Pattern.compile("(?<=\"district\":\")([^\"]*)").matcher(data);
        if (qu.find())
            student.setQu(qu.group());

    }

    public String qdSave(Student student, String cookie) {
        CloseableHttpResponse response;

        HttpHost proxyHost = new HttpHost("127.0.0.1", 8888);

        try (
                //CloseableHttpClient httpClient = HttpClients.custom().setProxy(proxyHost).setSSLContext(createUnverifiedSslContext()).build()
                CloseableHttpClient httpClient = HttpClients.createDefault()
        ) {

            HttpPost httpPost = new HttpPost("https://www.cdvisor.com:8443/4142012738/app/sxjy/s_sx_sxqd.xhtml?action=qdsave");
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=kfcCrazyTHvivo50");
            httpPost.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.47");
            httpPost.setHeader("Cookie", "userinfo=" + cookie);

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

            multipartEntityBuilder.setBoundary("kfcCrazyTHvivo50");
            multipartEntityBuilder.addTextBody("anquan", "安全", ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("jiankang", "健康", ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("jd", student.getLongitude().toString(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("wd", student.getLatitude().toString(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("atype", "1", ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("sheng", student.getSheng(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("shi", student.getShi(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("qu", student.getQu(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("location", student.getAddress(), ContentType.APPLICATION_JSON);
            multipartEntityBuilder.addTextBody("filenames", student.getPicture(), ContentType.APPLICATION_JSON);
            HttpEntity multipartEntity = multipartEntityBuilder.build();

            httpPost.setEntity(multipartEntity);

            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        HttpEntity responseEntity = response.getEntity();
        try {
            return EntityUtils.toString(responseEntity);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public String autoCheck(Student student) {
        String cookie = loginService.getCookie(student/*, loginService.getToken()*/);
        if (cookie.equals("")){
            studentService.deleteStudentByUserAndPass(student);
            return student.getUser() + ": 密码已更改!";
        }
        String s = qdSave(student, cookie);
        if (s.contains("1"))
            return student.getUser() + ": 签到成功";
        else if (s.contains("3"))
            return student.getUser() + ": 一天只能签到一次";
        else
            return student.getUser() + ": 签到失败";
    }

    public static SSLContext createUnverifiedSslContext() {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try {
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, new SecureRandom());
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
        return sslContext;
    }
}
