package com.github.drxaos.mmxweb.javacpp;

import com.github.drxaos.mmxweb.Webby;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FunctionPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
        public native int dispatchCallback(Request request, Connection connection);

        @Virtual
        public native int wsConnectCallback(Request request, Connection connection);

        @Virtual
        public native void wsConnectedCallback(Request request, Connection connection);

        @Virtual
        public native void wsDisconnectedCallback(Request request, Connection connection);

        @Virtual
        public native int wsFrameCallback(Request request, Frame frame, Connection connection);
    }

    @Name("wby_con")
    public static class Connection extends Pointer {
        static {
            Loader.load();
        }

        public Connection() {
            allocate();
        }

        private native void allocate();
    }

    @Name("wby_frame")
    public static class Frame extends Pointer {
        static {
            Loader.load();
        }

        public Frame() {
            allocate();
        }

        private native void allocate();
    }

    @Name("wby_request")
    public static class Request extends Pointer {
        static {
            Loader.load();
        }

        protected Connection con;

        public Request() {
            allocate();
        }

        public void setCon(Connection con) {
            this.con = con;
        }

        private native void allocate();

        @MemberGetter
        @Name("method")
        public native String getMethod();

        @MemberGetter
        @Name("uri")
        public native String getUri();

        @MemberGetter
        @Name("http_version")
        public native String getHttpVersion();

        @MemberGetter
        @Name("query_params")
        public native String getQueryParams();

        @MemberGetter
        @Name("content_length")
        public native int getContentLength();

        @MemberGetter
        @Name("header_count")
        public native int getHeaderCount();

        public Header getHeader(int index) {
            return get_header(this, index);
        }

        public String getHeaderValue(String name) {
            return wby_find_header(con, name);
        }

        public byte[] getBody() {
            return getBody(getContentLength());
        }

        public byte[] getBody(int maxLength) {
            BytePointer bytePointer = new BytePointer(maxLength);
            try {
                wby_read(con, bytePointer, maxLength);
                return bytePointer.getStringBytes();
            } finally {
                bytePointer.deallocate();
            }
        }

        public String getParameter(String name) {
            String queryParams = getQueryParams();
            if (queryParams == null || queryParams.isEmpty()) {
                return null;
            }
            BytePointer dst = new BytePointer(queryParams.length());
            try {
                int size = wby_find_query_var(queryParams, name, dst, queryParams.length());
                if (size < 0) {
                    return null;
                } else if (size == 0) {
                    return "";
                } else {
                    return new String(dst.getStringBytes(), 0, size);
                }
            } finally {
                dst.deallocate();
            }
        }
    }

    public static native Header get_header(Request req, int index);

    @Cast("wby_header*")
    public static native Pointer create_headers(int size);

    public static native void set_header(@Cast("wby_header*") Pointer headers, int index, String name, String value);

    public static native void delete_headers(@Cast("wby_header*") Pointer headers, int size);

    public static native int wby_read(Connection con, Pointer memory, int length);

    @Name("wby_header")
    public static class Header extends Pointer {
        static {
            Loader.load();
        }

        public Header() {
            allocate();
        }

        private native void allocate();

        @MemberGetter
        public native String name();

        @MemberSetter
        public native void name(String name);

        @MemberGetter
        public native String value();

        @MemberSetter
        public native void value(String value);
    }

    public static native int wby_response_begin(Connection con, int status_code, int content_length, @Cast("const wby_header*") Pointer headers, int header_count);

    public static native void wby_response_end(Connection con);

    public static native int wby_write(Connection con, Pointer memory, int length);

    public static native int wby_find_query_var(String buf, String name, @Cast("char*") BytePointer dst, int dstLength);

    public static native String wby_find_header(Connection connection, String name);

    public static class Response {
        protected Connection con;

        public void init(Connection con) {
            this.con = con;
            this.sent = false;
            this.ret = 0;
            this.status = 200;
            this.contentLength = -1;
            this.headers.clear();
            this.headers.put("server", "wby");
            this.headers.put("content-type", "text/plain");
        }

        int ret;
        int status;
        int contentLength;
        boolean sent = false;
        private Map<String, String> headers = new HashMap<String, String>();

        public int getRet() {
            return ret;
        }

        public void notFound() {
            this.ret = 1;
        }

        public void setStatus(int status) {
            if (sent) {
                throw new RuntimeException("Header already sent");
            }
            this.status = status;
        }

        public void setContentLength(int contentLength) {
            if (sent) {
                throw new RuntimeException("Header already sent");
            }
            this.contentLength = contentLength;
        }

        public void addHeader(String name, String value) {
            if (sent) {
                throw new RuntimeException("Header already sent");
            }
            headers.put(name.toLowerCase(), value);
        }

        public int beginResponse() {
            Pointer h = create_headers(headers.size());
            int count = 0;
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                set_header(h, count++, entry.getKey(), entry.getValue());
            }
            int res = wby_response_begin(con, status, contentLength, h, count);
            delete_headers(h, headers.size());
            sent = true;
            return res;
        }

        public void endResponse() {
            wby_response_end(con);
        }

        public int write(byte[] data) {
            BytePointer bp = new BytePointer(data);
            try {
                return wby_write(con, bp, data.length);
            } finally {
                bp.deallocate();
            }
        }

        public int write(byte[] data, int offset, int length) {
            BytePointer bp = new BytePointer(data);
            bp.position(offset);
            try {
                return wby_write(con, bp, length);
            } finally {
                bp.deallocate();
            }
        }
    }
}