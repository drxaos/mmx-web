package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

public interface WebbyDispatchHandler {
    void handle(WebbyBridge.Request request, WebbyBridge.Response response);
}
