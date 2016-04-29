package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Webby {
    private static final Log log = LogFactory.getLog(Webby.class);

    public void websocketBroadcast(String message) {
        WebbyBridge.websocket_broadcast_text(message);
    }

    public static void log(String message) {
        log.info(message);
    }
}
