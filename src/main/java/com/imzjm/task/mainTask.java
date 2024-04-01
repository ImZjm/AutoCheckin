package com.imzjm.task;

import com.imzjm.pojo.Student;
import com.imzjm.service.CheckinService;
import com.imzjm.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class mainTask {
    @Autowired
    StudentService studentService;
    @Autowired
    CheckinService checkinService;

    @Scheduled(cron = "1 18 6 * * ?")
    public void AutoBeiKun(){
        System.out.println("吉时已到!");
        //查找表中的所有student记录
        //遍历一遍，一个一个签到
        List<Student> allStudent = studentService.findAll();
        allStudent.forEach(student -> {
            String s = checkinService.autoCheck(student);
            System.out.println(s);
        });
    }
}
