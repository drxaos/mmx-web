package com.github.drxaos.mmxweb.websocket;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

abstract public class WebsocketBufferedManager extends WebsocketManager {

    ByteArrayOutputStream buf = new ByteArrayOutputStream();

    @Override
    public boolean frame(WebbyBridge.Frame frame, boolean fragment, boolean last, boolean binary) {
        if (fragment) {
            try {
                buf.write(frame.read());
            } catch (IOException inore) {
            }
        }

        if (last && fragment) {
            return frame(frame.getWsConnection(), buf.toByteArray(), binary);
        }

        if (last && !fragment) {
            return frame(frame.getWsConnection(), frame.read(), binary);
        }

        return true;
    }

    abstract public boolean frame(WebbyBridge.WsConnection wsConnection, byte[] data, boolean binary);

}
