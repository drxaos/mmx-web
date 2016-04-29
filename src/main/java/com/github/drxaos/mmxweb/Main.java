package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

public class Main {

    public static void main(String[] args) {
        WebbyBridge.callback(new WebbyBridge.Callback(){
            @Override
            public void log() {
                System.out.println("!!!!!!!");
            }
        });
    }
}
