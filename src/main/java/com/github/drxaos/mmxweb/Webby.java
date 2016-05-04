package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class Webby {
    private static final Log log = LogFactory.getLog(Webby.class);

    private WebbyBridge.Bridge bridge;

    private Thread loop;

    private AtomicBoolean quit = new AtomicBoolean(false);

    public static void log(String text) {
        log.info(text);
    }

    public void start(String host, int port) {
        bridge = new WebbyBridge.Bridge();
        bridge.configure(host, port);
        bridge.start();

        loop = new Thread("Webby loop (" + host + ":" + port + ")") {
            @Override
            public void run() {
                while (!quit.get()) {
                    bridge.update();
                    WebbyBridge.sleep_for(10);
                }
            }
        };
        loop.setDaemon(true);
        loop.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        bridge.stop();
    }
}
