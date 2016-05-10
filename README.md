# mmx-web
JavaCPP binding to mm_web (HTTP + WebSockets)

Webby is a web server intended for debugging tools inside a game or other program with a continously running main loop. It's intended to be used when all you need is something tiny and performance isn't a key concern.

Original code - https://github.com/deplinenoise/webby

Single header - https://github.com/vurtun/mmx

## Warning

This library uses native code and may crash your JVM. Do not use in production.

## Maven

https://github.com/drxaos/mvn-repo#mmx-web

## Example usage
```java
Webby webby = new Webby();

webby.setDispatchHandler(new RouteDispatcher()
      .route(new Route("/test") {
          @Override
          public void handle(WebbyBridge.Request request, WebbyBridge.Response response) {
              String name = request.getParameter("name");
              response.addHeader("Content-Type", "text/html; charset=UTF-8");
              response.beginResponse();
              response.write("Hello, " + name + "!");
              response.endResponse();
          }
      })
      .route(new StaticResourcesRoute("^/static/.+$"))
);

webby.setWebsocketHandler(new WebsocketMultiplexer()
        .manager(new WebsocketBufferedManager() {

            @Override
            public boolean canConnect(WebbyBridge.Request request) {
                return request.getUri().equals("/ws/echo");
            }

            @Override
            public boolean frame(WebbyBridge.WsConnection wsConnection, byte[] data, boolean binary) {
                String str = new String(data);
                sendText("Echo: " + str);
                broadcastText("example broadcast");
                return true;
            }
        })
);

webby.start();

// main loop
while (true) {
    webby.update();
    try {
        Thread.sleep(10);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```
