package com.newware.loginpage;

/**
 * Created by Bhuvaneshvar Nath Srivastava on 20-07-2018.
 * Copyright (c) 2018
 **/
public class User
{
    public String name, email, mobile;

    User()
    {
    }

    public User(String name, String email, String mobile)
    {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }
}
