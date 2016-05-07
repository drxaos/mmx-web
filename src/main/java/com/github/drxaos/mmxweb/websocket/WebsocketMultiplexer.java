package com.github.drxaos.mmxweb.websocket;

import com.github.drxaos.mmxweb.WebbyWebsocketHandler;
import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebsocketMultiplexer implements WebbyWebsocketHandler {
    List<WebsocketManager> managers = new ArrayList<>();

    Map<Long, WebsocketManager> map = new HashMap<>();

    public WebsocketMultiplexer manager(WebsocketManager manager) {
        managers.add(manager);
        return this;
    }

    WebsocketManager selectedManager;

    @Override
    public boolean connectRequest(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request) {
        for (WebsocketManager manager : managers) {
            if (manager.canConnect(request)) {
                selectedManager = manager;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConnected(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request) {
        map.put(wsConnection.getUid(), selectedManager);
        selectedManager.onConnected(wsConnection, request);
    }

    @Override
    public void onDisconnected(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request) {
        WebsocketManager manager = map.remove(wsConnection.getUid());
        manager.onDisconnected(wsConnection, request);
    }

    @Override
    public boolean onFrame(WebbyBridge.WsConnection wsConnection, WebbyBridge.Frame frame) {
        WebsocketManager manager = map.get(wsConnection.getUid());
        return manager.onFrame(wsConnection, frame);
    }
}
