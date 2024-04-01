package com.imzjm.dao;

import com.imzjm.pojo.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentDao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public int addStudent(Student student){
        String sql = "INSERT INTO studentlist (user, pass, longitude, latitude, sheng, shi, qu, address, picture) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Object[] objects = {
                student.getUser(),
                student.getPass(),
                student.getLongitude(),
                student.getLatitude(),
                student.getSheng(),
                student.getShi(),
                student.getQu(),
                student.getAddress(),
                student.getPicture()
        };
        return jdbcTemplate.update(sql, objects);
    }

    public int deleteByUserAndPass(Student student){
        String sql = "DELETE FROM studentlist WHERE user = ? AND pass = ?";
        return jdbcTemplate.update(sql, student.getUser(), student.getPass());
    }

    public Student findStudentByUser(Long user){
        String sql = "SELECT * FROM studentlist WHERE user = ?";
        RowMapper<Student> rowMapper = new BeanPropertyRowMapper<>(Student.class);
        return jdbcTemplate.queryForObject(sql, rowMapper, user);
    }

    public List<Student> findAllStudent(){
        String sql = "SELECT * FROM studentlist";
        RowMapper<Student> rowMapper = new BeanPropertyRowMapper<>(Student.class);
        return jdbcTemplate.query(sql, rowMapper);
    }

    public int updateStudent(Student student){
        String sql = "UPDATE studentlist SET pass = ?, longitude = ?, latitude = ?, sheng = ?, shi = ?, qu = ?, address = ?, picture = ? WHERE user = ?";
        Object[] objects = {
                student.getPass(),
                student.getLongitude(),
                student.getLatitude(),
                student.getSheng(),
                student.getShi(),
                student.getQu(),
                student.getAddress(),
                student.getPicture(),
                student.getUser()
        };
        return jdbcTemplate.update(sql, objects);
    }
}
