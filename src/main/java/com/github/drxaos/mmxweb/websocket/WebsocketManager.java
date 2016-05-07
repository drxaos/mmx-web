package com.github.drxaos.mmxweb.websocket;

import com.github.drxaos.mmxweb.WebbyWebsocketHandler;
import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

import java.util.ArrayList;
import java.util.List;

public abstract class WebsocketManager implements WebbyWebsocketHandler {

    protected List<Long> uids = new ArrayList<>();
    protected WebbyBridge.WsConnection currentConnection;

    @Override
    final public boolean connectRequest(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request) {
        currentConnection = wsConnection;
        return canConnect(request);
    }

    final public void onConnected(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request) {
        currentConnection = wsConnection;
        uids.add(wsConnection.getUid());
        connected(request);
    }

    @Override
    final public void onDisconnected(WebbyBridge.WsConnection wsConnection, WebbyBridge.Request request) {
        currentConnection = wsConnection;
        uids.remove(wsConnection.getUid());
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
                wsConnection.send(null, WebbyBridge.Frame.OPCODE_PONG);
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

    public void sendBinary(byte[] data) {
        currentConnection.sendBinary(data);
    }

    public void sendText(String text) {
        currentConnection.sendText(text);
    }

    public void broadcastBinary(byte[] data) {
        long current = currentConnection.getUid();
        for (Long uid : uids) {
            selectConnection(uid);
            currentConnection.sendBinary(data);
        }
        selectConnection(current);
    }

    public void broadcastText(String text) {
        long current = currentConnection.getUid();
        for (Long uid : uids) {
            selectConnection(uid);
            currentConnection.sendText(text);
        }
        selectConnection(current);
    }

    public void selectConnection(long uid) {
        currentConnection.selectConnection(uid);
    }

    public void disconnect() {
        currentConnection.close();
    }

    abstract public boolean canConnect(WebbyBridge.Request request);

    public void connected(WebbyBridge.Request request) {
    }

    public void disconnected(WebbyBridge.Request request) {
    }

    abstract public boolean frame(WebbyBridge.Frame frame, boolean fragment, boolean last, boolean binary);
}
