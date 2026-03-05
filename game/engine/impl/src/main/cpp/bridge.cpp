#include <jni.h>
#include <string>
#include <thread>
#include <cstdio>
#include <unistd.h>

#include "stockfish/bitboard.h"
#include "stockfish/endgame.h"
#include "stockfish/position.h"
#include "stockfish/search.h"
#include "stockfish/thread.h"
#include "stockfish/tt.h"
#include "stockfish/uci.h"

namespace PSQT { void init(); }

static int input_pipe[2]  = {-1, -1};
static int output_pipe[2] = {-1, -1};

static int saved_stdin  = -1;
static int saved_stdout = -1;

static FILE *stockfish_in  = nullptr;
static FILE *stockfish_out = nullptr;

static bool engine_running = false;

static void stockfish_main() {
    UCI::init(Options);
    PSQT::init();
    Bitboards::init();
    Position::init();
    Bitbases::init();
    Endgames::init();
    Threads.set(Options["Threads"]); // NOLINT(*-narrowing-conversions)
    Search::clear();

    // argc must be 1 so UCI::loop calls getline(cin, cmd) to read from stdin
    UCI::loop(1, nullptr);

    Threads.set(0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_paulcraciunas_game_engine_impl_StockfishBridge_nativeStartEngine(JNIEnv *, jobject) {
    if (engine_running) return;

    pipe(input_pipe);
    pipe(output_pipe);

    // Save original fds so they aren't leaked
    saved_stdin  = dup(STDIN_FILENO);
    saved_stdout = dup(STDOUT_FILENO);

    // Redirect stdin/stdout to our pipes
    dup2(input_pipe[0], STDIN_FILENO);
    dup2(output_pipe[1], STDOUT_FILENO);

    // Close the duplicated pipe ends that are now unused in this (Java) thread
    close(input_pipe[0]);
    close(output_pipe[1]);

    stockfish_in  = fdopen(input_pipe[1], "w");
    stockfish_out = fdopen(output_pipe[0], "r");

    // Disable buffering on the output reader so we get lines immediately
    setvbuf(stockfish_out, nullptr, _IONBF, 0);

    engine_running = true;
    std::thread(stockfish_main).detach();
}

extern "C" JNIEXPORT void JNICALL
Java_com_paulcraciunas_game_engine_impl_StockfishBridge_nativeShutdownEngine(JNIEnv *, jobject) {
    if (!engine_running) return;
    engine_running = false;

    // Close the write end of stdin pipe; forces EOF on Stockfish's stdin,
    // which causes UCI::loop to exit if it hasn't already from "quit".
    if (stockfish_in) {
        fclose(stockfish_in);
        stockfish_in = nullptr;
    }

    if (stockfish_out) {
        fclose(stockfish_out);
        stockfish_out = nullptr;
    }

    // Restore the original process-wide stdin/stdout so they aren't
    // left pointing at dead pipe descriptors.
    if (saved_stdin != -1) {
        dup2(saved_stdin, STDIN_FILENO);
        close(saved_stdin);
        saved_stdin = -1;
    }
    if (saved_stdout != -1) {
        dup2(saved_stdout, STDOUT_FILENO);
        close(saved_stdout);
        saved_stdout = -1;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_paulcraciunas_game_engine_impl_StockfishBridge_nativeSendCommand(
        JNIEnv *env, jobject, jstring jcmd) {
    const char *cmd = env->GetStringUTFChars(jcmd, nullptr);
    if (stockfish_in) {
        fprintf(stockfish_in, "%s\n", cmd);
        fflush(stockfish_in);
    }
    env->ReleaseStringUTFChars(jcmd, cmd);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_paulcraciunas_game_engine_impl_StockfishBridge_nativeReadOutput(JNIEnv *env, jobject) {
    char line[4096];
    if (stockfish_out && fgets(line, sizeof(line), stockfish_out)) {
        size_t len = strlen(line);
        if (len > 0 && line[len - 1] == '\n') {
            line[len - 1] = '\0';
        }
        return env->NewStringUTF(line);
    }
    return env->NewStringUTF("");
}
