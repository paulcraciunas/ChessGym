cmake_minimum_required(VERSION 3.18.1)

project("stockfishjni")

add_library(stockfishjni SHARED
    bridge.cpp
    stockfish/benchmark.cpp
    stockfish/bitbase.cpp
    stockfish/bitboard.cpp
    stockfish/endgame.cpp
    stockfish/evaluate.cpp
    stockfish/material.cpp
    stockfish/misc.cpp
    stockfish/movegen.cpp
    stockfish/movepick.cpp
    stockfish/pawns.cpp
    stockfish/position.cpp
    stockfish/psqt.cpp
    stockfish/search.cpp
    stockfish/thread.cpp
    stockfish/timeman.cpp
    stockfish/tt.cpp
    stockfish/uci.cpp
    stockfish/ucioption.cpp
    stockfish/syzygy/tbprobe.cpp
)

target_include_directories(stockfishjni PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/stockfish
)

target_compile_definitions(stockfishjni PRIVATE
    NDEBUG
    __ANDROID__
    NO_PREFETCH
)

if(CMAKE_SIZEOF_VOID_P EQUAL 8)
    target_compile_definitions(stockfishjni PRIVATE IS_64BIT USE_POPCNT)
endif()

target_compile_options(stockfishjni PRIVATE
    -std=c++11
    -O3
    -Wall
)
