package com.psgitech.library.model;

import com.psgitech.library.enums.Role;

/**
 * Student model extending User.
 * Demonstrates Inheritance and Polymorphism.
 */
public class Student extends User {
    private String registerNumber;
    private String department;
    private Integer year;

    public Student() {
        super();
        setRole(Role.STUDENT);
    }

    public Student(int userId, String name, String email, String passwordHash, String phone,
                   boolean active, String registerNumber, String department, Integer year) {
        super(userId, name, email, passwordHash, phone, Role.STUDENT, active);
        this.registerNumber = registerNumber;
        this.department = department;
        this.year = year;
    }

    @Override
    public String getIdentifier() {
        return registerNumber;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
