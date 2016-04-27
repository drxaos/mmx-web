package com.github.drxaos.mmxweb.javacpp;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.annotation.Platform;

@Platform(include = "MultiplyDemo.h")
public class MultiplyDemo {

    static {
        Loader.load();
    }

    public static native int multiply(int a, int b);

}