package com.github.drxaos.mmxweb.websocket;

import com.github.drxaos.mmxweb.WebbyWebsocketHandler;
import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class WebsocketManager implements WebbyWebsocketHandler {

    protected final Set<WebbyBridge.WsConnection> allConnections = Collections.synchronizedSet(new HashSet<WebbyBridge.WsConnection>());
    protected WebbyBridge.WsConnection currentConnection;

    @Override
    final public boolean connectRequest(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request) {
        currentConnection = wsConnection;
        return canConnect(request);
    }

    final public void onConnected(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request) {
        currentConnection = wsConnection;
        allConnections.add(wsConnection);
        connected(request);
    }

    @Override
    final public void onDisconnected(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request) {
        currentConnection = wsConnection;
        allConnections.remove(wsConnection);
        disconnected(request);
    }

    boolean binary;

    @Override
    final public boolean onFrame(WebbyBridge.WsConnection wsConnection, WebbyBridge.Frame frame) {
        currentConnection = wsConnection;
        boolean fin = frame.isFinal();

        switch (frame.getOpcode()) {
            case WebbyBridge.Frame.OPCODE_CLOSE:
                return false;
            case WebbyBridge.Frame.OPCODE_PING:
                // TODO send pong
                return true;
            case WebbyBridge.Frame.OPCODE_PONG:
                return true;
            case WebbyBridge.Frame.OPCODE_TEXT_FRAME:
                return frame(frame, !fin, fin, binary = false);
            case WebbyBridge.Frame.OPCODE_BINARY_FRAME:
                return frame(frame, !fin, fin, binary = true);
            case WebbyBridge.Frame.OPCODE_CONTINUATION:
                return frame(frame, true, fin, binary);
        }
        return true;
    }


    abstract public boolean canConnect(WebbyBridge.Request request);

    public void connected(WebbyBridge.Request request) {
    }

    public void disconnected(WebbyBridge.Request request) {
    }

    abstract public boolean frame(WebbyBridge.Frame frame, boolean fragment, boolean last, boolean binary);
}
