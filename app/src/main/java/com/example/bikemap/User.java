package com.example.bikemap;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String id;
    public String email;
    public String nickname;
    public String phonenumber;
    public double yellowSlope;
    public double redSlope;

    public User(){}

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPhonenumber(String phonenumber){
        this.phonenumber = phonenumber;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public User(String email, String phonenumber, String nickname) {
        this.email = email;
        this.phonenumber = phonenumber;
        this.nickname = nickname;
        this.yellowSlope = 4;
        this.redSlope = 8;
    }
}