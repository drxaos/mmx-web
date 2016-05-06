package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

public class Main {

    public static void main(String[] args) {
        Webby webby = new Webby();
        webby.setDispatchHandler(new WebbyDispatchHandler() {
            public void handle(WebbyBridge.Request request, WebbyBridge.Response response) {
                if (request.getUri().equals("/test")) {
                    String name = request.getParameter("name");
                    response.addHeader("From", "Robots");
                    response.addHeader("Server", "wobby");
                    response.addHeader("Content-Type", "text/html; charset=UTF-8");
                    response.beginResponse();
                    response.write(("Hello, " + name + "!").getBytes());
                    response.write(("<br/>" + request.getMethod() + " " + request.getUri() + "?" + request.getQueryParams() + " " + request.getHttpVersion()).getBytes());
                    for (int i = 0; i < request.getHeaderCount(); i++) {
                        WebbyBridge.Header header = request.getHeader(i);
                        response.write(("<br/>" + header.name() + ": " + header.value()).getBytes());
                    }
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
