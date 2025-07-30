package com.paulcraciunas.test.fakes

class FakePuzzleDatabase {
    fun foo() {
        TODO("Paul: Implement me")
    }
//    : PuzzleDatabase {
//    private var insertedId: Long = 0L
//
//    val insertedPuzzles = mutableListOf<Puzzle>()
//    var shouldThrowError = false
//
//    override suspend fun bulkInsert(all: List<Puzzle>) {
//        checkError()
//        insertedPuzzles.addAll(all)
//    }
//
//    override suspend fun insert(puzzle: Puzzle): Long {
//        checkError()
//        insertedPuzzles.add(puzzle)
//        return ++insertedId
//    }
//
//    override suspend fun get(count: Int): List<Puzzle> {
//        checkError()
//        return insertedPuzzles.subList(0, count)
//    }
//
//    override suspend fun getByRating(rating: Int): Puzzle? {
//        checkError()
//        return insertedPuzzles.firstOrNull { it.rating == rating }
//    }
//
//    override suspend fun getInRatingRange(min: Int, max: Int): Puzzle? {
//        checkError()
//        return insertedPuzzles.firstOrNull { it.rating in min..max }
//    }
//
//    private fun checkError() {
//        if (shouldThrowError) {
//            throw RuntimeException("Database error")
//        }
//    }
}
