package com.github.drxaos.mmxweb.route;

import com.github.drxaos.mmxweb.WebbyDispatchHandler;

abstract public class Route implements WebbyDispatchHandler {

    String prefix;

    public Route(String prefix) {
        this.prefix = prefix;
    }

}
