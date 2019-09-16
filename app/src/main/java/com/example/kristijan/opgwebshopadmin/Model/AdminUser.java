package com.example.kristijan.opgwebshopadmin.Model;

public class AdminUser {
    private String name;
    private String surname;
    private String phone;
    private String email;
    private int isAdmin;

    public AdminUser() {
    }

    public AdminUser(String email, int isAdmin) {
        this.email = email;
        this.isAdmin = isAdmin;
    }

    public AdminUser(String name, String surname, String phone, String email, int isAdmin) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
    }
}

