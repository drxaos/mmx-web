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

    private int connection_max = 8;
    private int request_buffer_size = 2048;
    private int io_buffer_size = 8096;

    private WebbyDispatchHandler dispatchHandler;

    public void setConnection_max(int connection_max) {
        this.connection_max = connection_max;
    }

    public void setRequest_buffer_size(int request_buffer_size) {
        this.request_buffer_size = request_buffer_size;
    }

    public void setIo_buffer_size(int io_buffer_size) {
        this.io_buffer_size = io_buffer_size;
    }

    public void setDispatchHandler(WebbyDispatchHandler dispatchHandler) {
        this.dispatchHandler = dispatchHandler;
    }

    public static void log(String text) {
        log.debug(text);
    }

    public void start(String host, int port) {
        bridge = new WebbyBridge.Bridge() {
            @Override
            public int dispatchCallback(WebbyBridge.Request request) {
                if (dispatchHandler != null) {
                    return dispatchHandler.handle(request);
                }
                return super.dispatchCallback(request);
            }
        };
        log.debug("Webby configure");
        bridge.configure(host, port, connection_max, request_buffer_size, io_buffer_size);
        log.debug("Webby start");
        bridge.start();

        loop = new Thread("Webby (" + host + ":" + port + ")") {
            @Override
            public void run() {
                log.debug("Webby loop start");
                while (!quit.get()) {
                    bridge.update();
                    WebbyBridge.sleep_for(10);
                }
                log.debug("Webby loop stop");
            }
        };
        loop.setDaemon(true);
        loop.start();
    }

    public void stop() {
        quit.set(true);
        try {
            loop.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bridge.stop();
        log.debug("Webby stopped");
    }
}
