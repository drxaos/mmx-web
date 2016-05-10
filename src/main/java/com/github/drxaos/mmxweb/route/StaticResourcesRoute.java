package com.github.drxaos.mmxweb.route;

import com.github.drxaos.mmxweb.javacpp.WebbyBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StaticResourcesRoute extends Route {
    private static final Logger log = LoggerFactory.getLogger(StaticResourcesRoute.class);

    ClassLoader classLoader;
    String path;

    public StaticResourcesRoute(String prefix, ClassLoader classLoader, String path) {
        super(prefix);
        init(classLoader, path);
    }

    public StaticResourcesRoute(String prefix, ClassLoader classLoader) {
        super(prefix);
        init(classLoader, "");
    }

    public StaticResourcesRoute(String prefix) {
        super(prefix);
        init(this.getClass().getClassLoader(), "");
    }

    protected void init(ClassLoader classLoader, String path) {
        this.classLoader = classLoader;
        this.path = path;
    }

    public static String getContentType(URL resource) {
        String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(resource.getFile());
        if (contentType == null) {
            try {
                Files.probeContentType(Paths.get(resource.toURI()));
            } catch (Exception ignore) {
            }
        }
        if (contentType == null) {
            contentType = "unknown";
        }
        return contentType;
    }

    @Override
    public void handleRequest(WebbyBridge.Request request, WebbyBridge.Response response) {
        String uri = request.getUri();
        URL resource = classLoader.getResource((path + uri).replaceFirst("^/+", ""));
        if (resource == null) {
            response.notFound();
            return;
        }

        response.addHeader("Content-Type", getContentType(resource));
        response.beginResponse();

        InputStream inputStream = null;
        try {
            inputStream = resource.openStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("failed to serve static resource: " + uri, e);
        }

        try {
            inputStream.close();
        } catch (Exception ignore) {
        }

        response.endResponse();
    }
}
