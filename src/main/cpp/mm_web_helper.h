
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define WBY_STATIC
#define WBY_IMPLEMENTATION
#define WBY_USE_FIXED_TYPES
#define WBY_USE_ASSERT
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

static void
sleep_for(long ms)
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

void log(const char* text) {
    logCallback(text);
}


//static int
//websocket_connect(struct wby_con *connection, void *userdata)
//{
//    /* Allow websocket upgrades on /wstest */
//    struct server_state *state = (struct server_state*)userdata;
//    /* connection bound userdata */
//    connection->user_data = NULL;
//    if (0 == strcmp(connection->request.uri, "/wstest") && state->conn_count < MAX_WSCONN)
//        return 0;
//    else return 1;
//}
//
//static void
//websocket_connected(struct wby_con *connection, void *userdata)
//{
//    struct server_state *state = (struct server_state*)userdata;
//    printf("WebSocket connected\n");
//    state->conn[state->conn_count++] = connection;
//}
//
//static int
//websocket_frame(struct wby_con *connection, const struct wby_frame *frame, void *userdata)
//{
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
//}
//
//static void
//websocket_closed(struct wby_con *connection, void *userdata)
//{
//    int i;
//    struct server_state *state = (struct server_state*)userdata;
//    printf("WebSocket closed\n");
//    for (i = 0; i < state->conn_count; i++) {
//        if (state->conn[i] == connection) {
//            int remain = state->conn_count - i;
//            memmove(state->conn + i, state->conn + i + 1, (size_t)remain * sizeof(struct wby_con*));
//            --state->conn_count;
//            break;
//        }
//    }
//}
//
//static void
//websocket_broadcast_text(const char* text)
//{
//    int i = 0;
//    for (i = 0; i < state.conn_count; ++i) {
//        wby_frame_begin(state.conn[i], WBY_WSOP_TEXT_FRAME);
//        wby_write(state.conn[i], text, strlen(text));
//        wby_frame_end(state.conn[i]);
//    }
//}

#define MAX_WSCONN 8
int dispatch(struct wby_con *connection, void *userdata);

class Bridge {
private:
    void *memory = NULL;
    wby_size needed_memory = 0;
    struct wby_server server;
    struct wby_config config;
// websockets
    struct wby_con *conn[MAX_WSCONN];
    int conn_count;
public:
    Bridge() {
    }

    ~Bridge() {
        if(config.address){
            free((void*)config.address);
        }
    }

    void configure(const char* host, int port) {
        memset(&config, 0, sizeof config);
        config.userdata = this;
        config.address = (const char*)malloc(strlen(host));
        strcpy((char*)config.address, (const char*)host);
        config.port = port;
        config.connection_max = 4;
        config.request_buffer_size = 2048;
        config.io_buffer_size = 8192;
        config.log = log;
        config.dispatch = dispatch;
//        config.ws_connect = websocket_connect;
//        config.ws_connected = websocket_connected;
//        config.ws_frame = websocket_frame;
//        config.ws_closed = websocket_closed;
    }

    void start() {
        #if defined(_WIN32)
            {WORD wsa_version = MAKEWORD(2,2);
            WSADATA wsa_data;
            if (WSAStartup(wsa_version, &wsa_data)) {
                fprintf(stderr, "WSAStartup failed\n");
                return 1;
            }}
        #endif

        wby_init(&server, &config, &needed_memory);
        memory = calloc(needed_memory, 1);
        wby_start(&server, memory);
    }

    void stop() {
        wby_stop(&server);
        free(memory);
        #if defined(_WIN32)
            WSACleanup();
        #endif
    }

    void update() {
        wby_update(&server);
    }

    virtual int dispatchCallback(struct wby_con *connection) {
        wby_response_begin(connection, 200, 14, NULL, 0);
        wby_write(connection, "Hello, world!\n", 14);
        wby_response_end(connection);
        return 0;
    }

};



int dispatch(struct wby_con *connection, void *userdata)
{
    struct Bridge *bridge = (Bridge*)userdata;
    return bridge->dispatchCallback(connection);
//
//    if (!strcmp("/foo", connection->request.uri)) {
//        wby_response_begin(connection, 200, 14, NULL, 0);
//        wby_write(connection, "Hello, world!\n", 14);
//        wby_response_end(connection);
//        return 0;
//    } else if (!strcmp("/bar", connection->request.uri)) {
//        wby_response_begin(connection, 200, -1, NULL, 0);
//        wby_write(connection, "Hello, world!\n", 14);
//        wby_write(connection, "Hello, world?\n", 14);
//        wby_response_end(connection);
//        return 0;
//    } else if (!strcmp("/quit", connection->request.uri)) {
//        wby_response_begin(connection, 200, -1, NULL, 0);
//        wby_write(connection, "Goodbye, cruel world\n", 22);
//        wby_response_end(connection);
//        //state->quit = 1;
//        return 0;
//    } else return 1;
}