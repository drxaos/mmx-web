package com.github.drxaos.mmxweb;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

public interface WebbyDispatchHandler {
    int handle(WebbyBridge.Request request);
}
