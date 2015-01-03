package com.sdl.MinetClient.gui;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import sun.audio.AudioData;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;

public class MusicPlayer {
    public static final String loginSound = "/resource/login.wav";
    public static final String logoutSound = "/resource/logout.wav";
    public static final String newMsgSound = "/resource/newMsg.wav";

    public static void login() {
        System.out.println("login");
        playOnce(loginSound);
    }

    public static void logout() {
        System.out.println("logout");
        playOnce(logoutSound);
    }

    public static void newMsg() {
        System.out.println("newMsg");
        playOnce(newMsgSound);
    }

    public static void playOnce(String fileName) {
        System.out.println("playOnce");
        InputStream in = null;
        AudioStream as = null;
        try {
            in = MusicPlayer.class.getResourceAsStream(fileName); // 打 开 一 个 声 音 文 件 流 作 为 输 入
        } catch (Exception ex) {
            // DO NOTHING
            System.out.println("Music Error1");
            ex.printStackTrace();
        }
        try {
            as = new AudioStream (in); // 用 输 入 流 创 建 一 个AudioStream 对 象 
        } catch (java.io.IOException ex) {
            // DO NOTHING
            System.out.println("Music Error2");
            ex.printStackTrace();
        }
        AudioPlayer.player.start (as); //“player” 是AudioPlayer 中 一 静 态 成 员 用 于 控 制 播 放         
    }
}