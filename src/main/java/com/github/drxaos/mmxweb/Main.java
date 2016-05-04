package com.github.drxaos.mmxweb;

public class Main {

    public static void main(String[] args) {
        Webby webby = new Webby();
        webby.start("127.0.0.1", 4444);
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
