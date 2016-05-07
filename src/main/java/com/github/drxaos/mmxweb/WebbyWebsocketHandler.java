package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

public interface WebbyWebsocketHandler {
    boolean connectRequest(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request);

    void onConnected(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request);

    void onDisconnected(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request);

    boolean onFrame(WebbyBridge.WsConnection wsConnection, WebbyBridge.Frame frame);
}
