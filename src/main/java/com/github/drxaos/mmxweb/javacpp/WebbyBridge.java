package com.github.drxaos.mmxweb.javacpp;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Virtual;

@Platform(include = "mm_web_helper.h")
public class WebbyBridge {

    static {
        Loader.load();
    }

    public static native void test_log(String msg);

    public static native void sleep_for(long ms);

    public static native void websocket_broadcast_text(String text);

    public static class Callback extends Pointer {
        static {
            Loader.load();
        }

        public Callback() {
            allocate();
        }

        private native void allocate();

        @Virtual
        public native void log(String text);
    }

    public static native void callback(Callback cb);

}