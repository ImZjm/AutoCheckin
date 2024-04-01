package com.imzjm.service;

import com.imzjm.dao.StudentDao;
import com.imzjm.pojo.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    @Autowired
    StudentDao studentDao;

    public void saveCheckInfo(Student student){
        //在保存数据前，先查询是否已有数据
        try {
            studentDao.findStudentByUser(student.getUser());
            //如果查询成功, 证明用户已存在, 执行更新操作
            studentDao.updateStudent(student);
        } catch (EmptyResultDataAccessException e) {
            //查询失败, 抛异常Incorrect result size: expected 1, actual 0
            //证明用户不存在
            studentDao.addStudent(student);
        }
    }

    public void deleteStudentByUserAndPass(Student student){
        studentDao.deleteByUserAndPass(student);
    }

    public List<Student> findAll(){
        return studentDao.findAllStudent();
    }

}
