package com.github.drxaos.mmxweb.javacpp;

import com.github.drxaos.mmxweb.Webby;
import org.bytedeco.javacpp.FunctionPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.annotation.Name;
import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Virtual;

@Platform(include = "mm_web_helper.h")
public class WebbyBridge {

    static {
        Loader.load();

    }

    public static class LogCallback extends FunctionPointer {
        static {
            Loader.load();
        }

        protected LogCallback() {
            allocate();
        }

        private native void allocate();

        @Name("logCallback")
        public void call(String text) throws Exception {
            Webby.log(text);
        }
    }

    public static native void sleep_for(long ms);

    public static native void log(String text);

    public static class Bridge extends Pointer {
        static {
            Loader.load();
        }

        public Bridge() {
            allocate();
        }

        private native void allocate();

        public native void configure(String host, int port, int connection_max, int request_buffer_size, int io_buffer_size);

        public native void start();

        public native void stop();

        public native void update();

        @Virtual
        public native int dispatchCallback(Request request);
    }

    @Name("wby_request")
    public static class Request extends Pointer {
        static {
            Loader.load();
        }

        public Request() {
            allocate();
        }

        private native void allocate();

        public native String method();

        public native void method(String property);

        public native String uri();

        public native void uri(String property);

        public native String http_version();

        public native void http_version(String property);

        public native String query_params();

        public native void query_params(String property);

        public native int content_length();

        public native void content_length(int property);

        public native int header_count();

        public native void header_count(int property);

        // TODO headers
    }
}