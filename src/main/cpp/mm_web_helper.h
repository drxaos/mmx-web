
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define WBY_STATIC
#define WBY_IMPLEMENTATION
#define WBY_USE_FIXED_TYPES
#define WBY_USE_ASSERT
#define WBY_UINT_PTR size_t
#include "mm_web.h"

#ifdef _WIN32
#include <winsock2.h>
#endif

#ifdef __APPLE__
#include <unistd.h>
#endif


extern "C" {
    JNIEXPORT void logCallback(const char*);
}

void log(const char* text) {
    logCallback(text);
}


static void sleep_for(long ms)
{
#if defined(__APPLE__)
    usleep(ms * 1000);
#elif defined(_WIN32)
    Sleep(ms);
#else
    time_t sec = (int)(ms / 1000);
    const long t = ms -(sec * 1000);
    struct timespec req;
    req.tv_sec = sec;
    req.tv_nsec = t * 1000000L;
    while(-1 == nanosleep(&req, &req));
#endif
}

int dispatch(struct wby_con *connection, void *userdata);
int websocket_connect(struct wby_con *connection, void *userdata);
void websocket_connected(struct wby_con *connection, void *userdata);
void websocket_closed(struct wby_con *connection, void *userdata);
int websocket_frame(struct wby_con *connection, const struct wby_frame *frame, void *userdata);

class Bridge {

private:
    void *memory = NULL;
    wby_size needed_memory = 0;
    struct wby_server server;
    struct wby_config config;
public:
    Bridge() {}
    ~Bridge() {}

    wby_con* findCon(long uid) {
        for (int i = 0, count = server.con_count; i < count; ++i) {
            struct wby_connection *conn = &server.con[i];
            if (uid == (long) conn->socket)
            {
                return &conn->public_data;
            }
        }
    }

    void configure(const char* host, int port, int connection_max, int request_buffer_size, int io_buffer_size) {
        memset(&config, 0, sizeof config);
        config.userdata = this;
        config.address = (const char*)malloc(strlen(host)+1);
        strcpy((char*)config.address, (const char*)host);
        config.port = port;
        config.connection_max = connection_max;
        config.request_buffer_size = request_buffer_size;
        config.io_buffer_size = io_buffer_size;
        config.log = log;
        config.dispatch = dispatch;
        config.ws_connect = websocket_connect;
        config.ws_connected = websocket_connected;
        config.ws_frame = websocket_frame;
        config.ws_closed = websocket_closed;
    }

    int start() {
        #if defined(_WIN32)
            {WORD wsa_version = MAKEWORD(2,2);
            WSADATA wsa_data;
            if (WSAStartup(wsa_version, &wsa_data)) {
                log("WSAStartup failed");
                return 1;
            }}
        #endif

        wby_init(&server, &config, &needed_memory);
        memory = calloc(needed_memory, 1);
        return wby_start(&server, memory);
    }

    void stop() {
        wby_stop(&server);
        free(memory);
        #if defined(_WIN32)
            WSACleanup();
        #endif
        if(config.address) {
            free((void*)config.address);
        }
    }

    void update() {
        wby_update(&server);
    }

    virtual int dispatchCallback(struct wby_request *request, struct wby_con *con) {
        return 1;
    }

    virtual int wsConnectCallback(struct wby_request *request, struct wby_con *con) {
        return 1;
    }

    virtual void wsConnectedCallback(struct wby_request *request, struct wby_con *con) {
    }

    virtual void wsDisconnectedCallback(struct wby_request *request, struct wby_con *con) {
    }

    virtual int wsFrameCallback(struct wby_frame *frame, struct wby_con *con) {
        return 1;
    }
};

int dispatch(struct wby_con *connection, void *userdata)
{
    struct Bridge *bridge = (Bridge*)userdata;
    return bridge->dispatchCallback(&connection->request, connection);
}

int websocket_connect(struct wby_con *connection, void *userdata)
{
    struct Bridge *bridge = (Bridge*)userdata;
    return bridge->wsConnectCallback(&connection->request, connection);
}

void websocket_connected(struct wby_con *connection, void *userdata)
{
    struct Bridge *bridge = (Bridge*)userdata;
    bridge->wsConnectedCallback(&connection->request, connection);
}

void websocket_closed(struct wby_con *connection, void *userdata)
{
    struct Bridge *bridge = (Bridge*)userdata;
    bridge->wsDisconnectedCallback(&connection->request, connection);
}

int websocket_frame(struct wby_con *connection, const struct wby_frame *frame, void *userdata)
{
    struct Bridge *bridge = (Bridge*)userdata;
    return bridge->wsFrameCallback((wby_frame*)frame, connection);

//    int i = 0;
//    printf("WebSocket frame incoming\n");
//    printf("  Frame OpCode: %d\n", frame->opcode);
//    printf("  Final frame?: %s\n", (frame->flags & WBY_WSF_FIN) ? "yes" : "no");
//    printf("  Masked?     : %s\n", (frame->flags & WBY_WSF_MASKED) ? "yes" : "no");
//    printf("  Data Length : %d\n", (int) frame->payload_length);
//    while (i < frame->payload_length) {
//        unsigned char buffer[16];
//        int remain = frame->payload_length - i;
//        size_t read_size = remain > (int) sizeof buffer ? sizeof buffer : (size_t) remain;
//        size_t k;
//
//        printf("%08x ", (int) i);
//        if (0 != wby_read(connection, buffer, read_size))
//            break;
//        for (k = 0; k < read_size; ++k)
//            printf("%02x ", buffer[k]);
//        for (k = read_size; k < 16; ++k)
//            printf("   ");
//        printf(" | ");
//        for (k = 0; k < read_size; ++k)
//            printf("%c", isprint(buffer[k]) ? buffer[k] : '?');
//        printf("\n");
//        i += (int)read_size;
//    }
//    return 0;
}

int get_max_headers(){
    return WBY_MAX_HEADERS;
}

wby_header* get_header(wby_request* req, int index) {
    return &req->headers[index];
}


wby_header* create_headers(int size) {
    return new wby_header[size];
}

void delete_headers(wby_header* h, int size) {
    for(int i = 0; i < size; i++){
        free((void*)h[i].name);
        free((void*)h[i].value);
    }
    return delete[] h;
}

void set_header(wby_header* header, int index, const char* name, const char* value) {
    const char* k = (const char*)malloc(strlen(name)+1);
    strcpy((char*)k, (const char*)name);
    const char* v = (const char*)malloc(strlen(value)+1);
    strcpy((char*)v, (const char*)value);
    header[index].name = k;
    header[index].value = v;
}

char* get_error_message() {
    return strerror(wby_socket_error());
}

long wby_con_uid(wby_con *con) {
    return (int)((struct wby_connection*)con)->socket;
}

wby_con* wby_uid_con(Bridge *bridge, long uid) {
    return bridge->findCon(uid);
}
