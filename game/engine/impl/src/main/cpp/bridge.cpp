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

static int input_pipe[2];
static int output_pipe[2];
static FILE *stockfish_in = nullptr;
static FILE *stockfish_out = nullptr;

static void stockfish_main() {
    UCI::init(Options);
    PSQT::init();
    Bitboards::init();
    Position::init();
    Bitbases::init();
    Endgames::init();
    Threads.set(Options["Threads"]);
    Search::clear();

    UCI::loop(0, nullptr);

    Threads.set(0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_paulcraciunas_game_engine_impl_StockfishBridge_nativeStartEngine(JNIEnv *, jobject) {
    pipe(input_pipe);
    pipe(output_pipe);

    dup2(input_pipe[0], STDIN_FILENO);
    dup2(output_pipe[1], STDOUT_FILENO);

    stockfish_in = fdopen(input_pipe[1], "w");
    stockfish_out = fdopen(output_pipe[0], "r");

    std::thread(stockfish_main).detach();
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
