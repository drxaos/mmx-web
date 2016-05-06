package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

public interface WebbyWebsocketHandler {
    boolean connectRequest(WebbyBridge.Request request);

    void onConnected(WebbyBridge.Request request);

    void onDisconnected(WebbyBridge.Request request);

    boolean onFrame(WebbyBridge.Request request, WebbyBridge.Frame frame);
}
