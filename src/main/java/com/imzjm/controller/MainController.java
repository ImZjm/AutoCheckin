package com.imzjm.controller;

import com.imzjm.entity.Result;
import com.imzjm.pojo.Student;
import com.imzjm.service.StudentService;
import com.imzjm.service.CheckinService;
import com.imzjm.service.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @Autowired
    LoginService loginService;
    @Autowired
    CheckinService checkinService;
    @Autowired
    StudentService studentService;

    @PostMapping("/login")
    public Result login(HttpServletResponse response, Student student) {
        //判断账号或密码是否为空
        if (student.getUser() == null || student.getPass().equals("d41d8cd98f00b204e9800998ecf8427e")) {
            return new Result(false, "请输入账号或密码！");
        }

        //String token = loginService.getToken();
        //if (token.equals(""))
        //    return new Result(false, "系统内部错误！");


        String cookie = loginService.getCookie(student);
        if (cookie.equals(""))
            return new Result(false, "账号或密码错误！");
        Cookie userinfo = new Cookie("userinfo", cookie);
        response.addCookie(userinfo);
        return new Result(true, "登录成功");
    }

    @PostMapping("/checkin")
    public Result checkIn(@CookieValue("userinfo") String userinfo, Student student){
        if (student.getLatitude() == null || student.getLongitude() == null || student.getAddress().equals(""))
            return new Result(false, "数据有误!");
        checkinService.updateCheckInfo(student);

        String code = checkinService.qdSave(student, userinfo);
        if (code.contains("1")){
            if (student.isAutoCheck())
                studentService.saveCheckInfo(student);
            return new Result(true, "签到成功！");
        }
        else if (code.contains("3")) {
            if (student.isAutoCheck())
                studentService.saveCheckInfo(student);
            return new Result(false, "一天只能签到一次！");
        }
        else
            return new Result(false, "签到失败！");
    }
}
