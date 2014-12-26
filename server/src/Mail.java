package com.sdl.MiroServer;

public class  Mail{
    public String sender;
    public String date;
    public String text;
    public String title;

    public Mail(String s_, String d_, String ti_, String te_) {
        sender = s_;
        date = d_;
        text = te_;
        title = ti_;
    }
}