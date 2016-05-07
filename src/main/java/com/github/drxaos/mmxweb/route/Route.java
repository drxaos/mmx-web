package com.github.drxaos.mmxweb.route;

import com.github.drxaos.mmxweb.WebbyDispatchHandler;

import java.util.Objects;

abstract public class Route implements WebbyDispatchHandler {

    public static final int EQUALS = 1;
    public static final int STARTS = 2;
    public static final int MATCHES = 3;

    String matcher;
    int type;

    public Route(String matcher) {
        this.matcher = matcher;
        this.type = MATCHES;
    }

    public Route(String matcher, int type) {
        this.matcher = matcher;
        this.type = type;
    }

    public boolean canHandle(String uri) {
        switch (type) {
            case EQUALS:
                return Objects.equals(uri, this.matcher);
            case STARTS:
                return uri != null && uri.startsWith(this.matcher);
            case MATCHES:
                return uri != null && uri.matches(this.matcher);
            default:
                return false;
        }
    }

}
