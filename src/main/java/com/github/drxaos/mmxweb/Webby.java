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
    private boolean started;

    private AtomicBoolean quit = new AtomicBoolean(false);
    private AtomicInteger autoUpdateInterval = new AtomicInteger(15);

    private int maxConnections = 8;
    private int requestBufferSize = 2048;
    private int ioBufferSize = 8096;

    private int port = 4444;
    private String ip = "127.0.0.1";

    private WebbyDispatchHandler dispatchHandler;
    private WebbyWebsocketHandler websocketHandler;

    public Webby() {
    }

    public Webby(String ip, int port) {
        this.port = port;
        this.ip = ip;
    }

    public Webby setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public Webby setRequestBufferSize(int requestBufferSize) {
        this.requestBufferSize = requestBufferSize;
        return this;
    }

    public Webby setIoBufferSize(int ioBufferSize) {
        this.ioBufferSize = ioBufferSize;
        return this;
    }

    public Webby setPort(int port) {
        this.port = port;
        return this;
    }

    public Webby setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Webby setDispatchHandler(WebbyDispatchHandler dispatchHandler) {
        this.dispatchHandler = dispatchHandler;
        return this;
    }

    public Webby setWebsocketHandler(WebbyWebsocketHandler websocketHandler) {
        this.websocketHandler = websocketHandler;
        return this;
    }

    public static void log(String text) {
        log.debug(text);
    }

    public void start() {
        if (started) {
            throw new WebbyException("Cannot start twice, use another instance");
        }
        started = true;

        bridge = new WebbyBridge.Bridge() {
            WebbyBridge.Response response = new WebbyBridge.Response();
            WebbyBridge.WsConnection wsConnection = new WebbyBridge.WsConnection(this);

            @Override
            public int dispatchCallback(WebbyBridge.Request request, WebbyBridge.Connection connection) {
                try {
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
                } catch (Exception e) {
                    log.error("dispatch handler error", e);
                }
                return super.dispatchCallback(request, connection);
            }

            @Override
            public int wsConnectCallback(WebbyBridge.Request request, WebbyBridge.Connection connection) {
                try {
                    if (websocketHandler != null) {
                        request.init(connection, true);
                        wsConnection.init(connection);
                        try {
                            return websocketHandler.connectRequest(wsConnection, request) ? 0 : 1;
                        } catch (Throwable t) {
                            log.error("ws error", t);
                            return 1;
                        }
                    }
                } catch (Exception e) {
                    log.error("websocket handler error", e);
                }
                return super.wsConnectCallback(request, connection);
            }

            @Override
            public void wsConnectedCallback(WebbyBridge.Request request, WebbyBridge.Connection connection) {
                try {
                    if (websocketHandler != null) {
                        request.init(connection, true);
                        wsConnection.init(connection);
                        try {
                            websocketHandler.onConnected(wsConnection, request);
                            return;
                        } catch (Throwable t) {
                            log.error("ws error", t);
                            return;
                        }
                    }
                } catch (Exception e) {
                    log.error("websocket handler error", e);
                }
                super.wsConnectedCallback(request, connection);
            }

            @Override
            public void wsDisconnectedCallback(WebbyBridge.Request request, WebbyBridge.Connection connection) {
                try {
                    if (websocketHandler != null) {
                        request.init(connection, true);
                        wsConnection.init(connection);
                        try {
                            websocketHandler.onDisconnected(wsConnection, request);
                            return;
                        } catch (Throwable t) {
                            log.error("ws error", t);
                            return;
                        }
                    }
                } catch (Exception e) {
                    log.error("websocket handler error", e);
                }
                super.wsDisconnectedCallback(request, connection);
            }

            @Override
            public int wsFrameCallback(WebbyBridge.Frame frame, WebbyBridge.Connection connection) {
                try {
                    if (websocketHandler != null) {
                        wsConnection.init(connection);
                        frame.init(wsConnection);
                        try {
                            return websocketHandler.onFrame(wsConnection, frame) ? 0 : 1;
                        } catch (Throwable t) {
                            log.error("ws error", t);
                            return 1;
                        }
                    }
                } catch (Exception e) {
                    log.error("websocket handler error", e);
                }
                return super.wsFrameCallback(frame, connection);
            }
        };
        log.debug("Webby configure");
        bridge.configure(ip, port, maxConnections, requestBufferSize, ioBufferSize);
        log.debug("Webby start");
        int err = bridge.start();
        if (err != 0) {
            throw new WebbyException(WebbyBridge.get_error_message());
        }
        log.debug("Webby started");
    }

    public void update() {
        if (!started) {
            start();
        }
        bridge.update();
    }

    public Webby startAutoUpdate(final int intervalMs) {
        if (!started) {
            start();
        }
        quit.set(false);
        autoUpdateInterval.set(intervalMs);
        if (loop == null || !loop.isAlive()) {
            loop = new Thread("Webby (" + ip + ":" + port + ")") {
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
        return this;
    }

    public Webby startAutoUpdateAndJoin(final int intervalMs) {
        startAutoUpdate(intervalMs);
        try {
            loop.join();
        } catch (InterruptedException ignore) {
        }
        return this;
    }

    public Webby stopAutoUpdate() {
        quit.set(true);
        return this;
    }

    public Webby stop() {
        if (!started) {
            return this;
        }
        quit.set(true);
        try {
            loop.join(1000);
        } catch (InterruptedException ignore) {
        }
        bridge.stop();
        log.debug("Webby stopped");
        return this;
    }
}
