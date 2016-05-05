package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

public class Main {

    public static void main(String[] args) {
        Webby webby = new Webby();
        webby.setDispatchHandler(new WebbyDispatchHandler() {
            public void handle(WebbyBridge.Request request, WebbyBridge.Response response) {
                System.out.println("" + request.getMethod() + " " + request.getUri() + "?" + request.getQueryParams() + " " + request.getHttpVersion());
                for (int i = 0; i < request.getHeaderCount(); i++) {
                    WebbyBridge.Header header = request.getHeader(i);
                    System.out.println("" + header.name() + ": " + header.value());
                }
                System.out.println();

                if (request.getUri().equals("/test")) {
                    String name = "human";
                    String params = request.getQueryParams();
                    if (params != null) {
                        for (String kv : params.split("\\&")) {
                            String[] pair = kv.split("=", 2);
                            if (pair[0].equals("name")) {
                                name = pair[1];
                            }
                        }
                    }
                    response.addHeader("From", "Robots");
                    response.addHeader("Server", "wobby");
                    response.beginResponse();
                    response.write(("Hello, " + name + "!").getBytes());
                    response.endResponse();
                } else {
                    response.notFound();
                }

            }
        });
        webby.start();
        while (true) {
            webby.update();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
