package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

public class Main {

    public static void main(String[] args) {
        Webby webby = new Webby();
        webby.setDispatchHandler(new WebbyDispatchHandler() {
            public int handle(WebbyBridge.Request request) {
                return 1;
            }
        });
        webby.start("127.0.0.1", 4444);
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        webby.stop();
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
