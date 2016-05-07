package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;
import com.github.drxaos.mmxweb.route.Route;
import com.github.drxaos.mmxweb.route.RouteDispatcher;
import com.github.drxaos.mmxweb.route.StaticResourcesRoute;
import com.github.drxaos.mmxweb.websocket.WebsocketBufferedManager;
import com.github.drxaos.mmxweb.websocket.WebsocketMultiplexer;

public class Main {

    public static void main(String[] args) {
        Webby webby = new Webby();

        webby.setDispatchHandler(new RouteDispatcher()
                .route(new Route("/test") {
                    @Override
                    public void handle(WebbyBridge.Request request, WebbyBridge.Response response) {
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
                    }
                })
                .route(new StaticResourcesRoute("^/static/.+$"))
        );

        webby.setWebsocketHandler(new WebsocketMultiplexer()
                .manager(new WebsocketBufferedManager() {

                    @Override
                    public boolean canConnect(WebbyBridge.Request request) {
                        return request.getUri().equals("/ws/echo");
                    }

                    @Override
                    public boolean frame(WebbyBridge.WsConnection wsConnection, byte[] data, boolean binary) {
                        int sum = 0;
                        for (byte b : data) {
                            sum += b;
                        }
                        String str = new String(data);
                        System.out.println("Data: " + str);
                        wsConnection.sendText(str + " / SUM: " + sum);
                        return true;
                    }
                })
                .manager(new WebsocketBufferedManager() {

                    @Override
                    public boolean canConnect(WebbyBridge.Request request) {
                        return request.getUri().equals("/ws/time");
                    }

                    @Override
                    public void connected(WebbyBridge.Request request) {
                        currentConnection.sendText("Current timestamp: " + System.currentTimeMillis());
                        currentConnection.close();
                    }

                    @Override
                    public boolean frame(WebbyBridge.WsConnection wsConnection, byte[] data, boolean binary) {
                        return false;
                    }
                })

        );

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
