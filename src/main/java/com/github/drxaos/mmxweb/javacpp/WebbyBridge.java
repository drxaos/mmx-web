package com.github.drxaos.mmxweb.javacpp;

import com.github.drxaos.mmxweb.Webby;
import org.bytedeco.javacpp.FunctionPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.annotation.Name;
import org.bytedeco.javacpp.annotation.Platform;

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

        public native void configure(String host, int port);

        public native void start();

        public native void stop();

        public native void update();
    }

}