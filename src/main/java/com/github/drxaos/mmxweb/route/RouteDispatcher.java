package com.github.drxaos.mmxweb.route;

import com.github.drxaos.mmxweb.WebbyDispatchHandler;
import com.github.drxaos.mmxweb.javacpp.WebbyBridge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class RouteDispatcher implements WebbyDispatchHandler {

    List<Route> routes = new ArrayList<>();

    public RouteDispatcher addRoute(Route route) {
        routes.add(route);
        return this;
    }

    public RouteDispatcher removeRoute(Route route) {
        routes.remove(route);
        return this;
    }

    public void removeRoute(String prefix) {
        for (Iterator<Route> iterator = routes.iterator(); iterator.hasNext(); ) {
            Route r = iterator.next();
            if (Objects.equals(r.prefix, prefix)) {
                iterator.remove();
            }
        }
    }

    @Override
    public void handle(WebbyBridge.Request request, WebbyBridge.Response response) {
        String uri = request.getUri();
        for (Route route : routes) {
            if (route.prefix != null && uri.startsWith(route.prefix)) {
                route.handle(request, response);
                return;
            }
        }
        response.notFound();
    }
}
