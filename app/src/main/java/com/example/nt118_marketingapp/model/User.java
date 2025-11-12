package com.example.nt118_marketingapp.model;

public class User {

    private String UserId;
    private String FullName;
    private String Email;
    private String Phone;

    private String RoleName;

    public User(String UserId ,String fullName, String roleName, String email, String phone) {
        this.UserId = UserId;
        FullName = fullName;
        RoleName = roleName;
        Email = email;
        Phone = phone;
    }

    public String getUserId() {
        return UserId;
    }

    public String getFullName() {
        return FullName;
    }

    public String getEmail() {
        return Email;
    }

    public String getPhone() {
        return Phone;
    }

    public String getRoleName() {
        return RoleName;
    }

}

