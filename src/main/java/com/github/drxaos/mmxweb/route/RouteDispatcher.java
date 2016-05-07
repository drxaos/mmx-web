package com.github.drxaos.mmxweb.route;

import com.github.drxaos.mmxweb.WebbyDispatchHandler;
import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

import java.util.ArrayList;
import java.util.List;

public class RouteDispatcher implements WebbyDispatchHandler {

    List<Route> routes = new ArrayList<>();

    public RouteDispatcher route(Route route) {
        routes.add(route);
        return this;
    }

    @Override
    public void handle(WebbyBridge.Request request, WebbyBridge.Response response) {
        String uri = request.getUri();
        for (Route route : routes) {
            if (route.canHandle(uri)) {
                route.handle(request, response);
                return;
            }
        }
        response.notFound();
    }
}
