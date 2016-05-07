package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Webby {
    private static final Logger log = LoggerFactory.getLogger(Webby.class);

    private WebbyBridge.Bridge bridge;

    private Thread loop;

    private AtomicBoolean quit = new AtomicBoolean(false);
    private AtomicInteger autoUpdateInterval = new AtomicInteger(15);

    private int maxConnections = 8;
    private int requestBufferSize = 2048;
    private int ioBufferSize = 8096;

    private int port = 4444;
    private String host = "127.0.0.1";

    private WebbyDispatchHandler dispatchHandler;
    private WebbyWebsocketHandler websocketHandler;

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public void setRequestBufferSize(int requestBufferSize) {
        this.requestBufferSize = requestBufferSize;
    }

    public void setIoBufferSize(int ioBufferSize) {
        this.ioBufferSize = ioBufferSize;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setDispatchHandler(WebbyDispatchHandler dispatchHandler) {
        this.dispatchHandler = dispatchHandler;
    }

    public void setWebsocketHandler(WebbyWebsocketHandler websocketHandler) {
        this.websocketHandler = websocketHandler;
    }

    public static void log(String text) {
        log.debug(text);
    }

    public void start() {
        bridge = new WebbyBridge.Bridge() {
            WebbyBridge.Response response = new WebbyBridge.Response();

            @Override
            public int dispatchCallback(WebbyBridge.Request request, WebbyBridge.Connection connection) {
                if (dispatchHandler != null) {
                    request.init(connection, false);
                    response.init(connection);
                    try {
                        dispatchHandler.handle(request, response);
                    } catch (Throwable t) {
                        log.error("dispatcher error", t);
                        response.setStatus(500);
                        response.beginResponse();
                        response.write(("Server error").getBytes());
                        response.endResponse();
                    }
                    return response.getRet();
                }
                return super.dispatchCallback(request, connection);
            }

            @Override
            public int wsConnectCallback(WebbyBridge.Request request, WebbyBridge.Connection connection) {
                if (websocketHandler != null) {
                    request.init(connection, true);
                    try {
                        return websocketHandler.connectRequest(connection.getWsConnection(), request) ? 0 : 1;
                    } catch (Throwable t) {
                        log.error("ws error", t);
                        return 1;
                    }
                }
                return super.wsConnectCallback(request, connection);
            }

            @Override
            public void wsConnectedCallback(WebbyBridge.Request request, WebbyBridge.Connection connection) {
                if (websocketHandler != null) {
                    request.init(connection, true);
                    try {
                        websocketHandler.onConnected(connection.getWsConnection(), request);
                        return;
                    } catch (Throwable t) {
                        log.error("ws error", t);
                        return;
                    }
                }
                super.wsConnectedCallback(request, connection);
            }

            @Override
            public void wsDisconnectedCallback(WebbyBridge.Request request, WebbyBridge.Connection connection) {
                if (websocketHandler != null) {
                    request.init(connection, true);
                    try {
                        websocketHandler.onDisconnected(connection.getWsConnection(), request);
                        return;
                    } catch (Throwable t) {
                        log.error("ws error", t);
                        return;
                    }
                }
                super.wsDisconnectedCallback(request, connection);
            }

            @Override
            public int wsFrameCallback(WebbyBridge.Frame frame, WebbyBridge.Connection connection) {
                if (websocketHandler != null) {
                    frame.init(connection.getWsConnection());
                    try {
                        return websocketHandler.onFrame(connection.getWsConnection(), frame) ? 0 : 1;
                    } catch (Throwable t) {
                        log.error("ws error", t);
                        return 1;
                    }
                }
                return super.wsFrameCallback(frame, connection);
            }
        };
        log.debug("Webby configure");
        bridge.configure(host, port, maxConnections, requestBufferSize, ioBufferSize);
        log.debug("Webby start");
        int err = bridge.start();
        if (err != 0) {
            throw new WebbyException(WebbyBridge.get_error_message());
        }
        log.debug("Webby started");
    }

    public void update() {
        bridge.update();
    }

    public void startAutoUpdate(final int intervalMs) {
        quit.set(false);
        autoUpdateInterval.set(intervalMs);
        if (loop == null || !loop.isAlive()) {
            loop = new Thread("Webby (" + host + ":" + port + ")") {
                @Override
                public void run() {
                    log.debug("Webby loop start");
                    while (!quit.get()) {
                        bridge.update();
                        WebbyBridge.sleep_for(autoUpdateInterval.get());
                    }
                    log.debug("Webby loop stop");
                }
            };
            loop.setDaemon(true);
            loop.start();
        }
    }

    public void stopAutoUpdate() {
        quit.set(true);
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
