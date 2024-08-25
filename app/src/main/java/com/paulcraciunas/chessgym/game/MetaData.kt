package com.paulcraciunas.chessgym.game

/**
 * Information about a Game. This is often present in games imported from PGN format
 *
 * @see <a href="https://en.wikipedia.org/wiki/Portable_Game_Notation#Seven_Tag_Roster">
 *     Seven Tag Roster</a>
 */
data class MetaData(val headers: Map<Header, String> = mapOf()) {

    fun data(header: Header): String? = headers[header]

    enum class Header(val key: String) {
        Event("Event"),
        Site("Site"),
        Date("Date"),
        Round("Round"),
        White("White"),
        Black("Black"),
        Result("Result");

        companion object {
            fun of(value: String): Header? = entries.firstOrNull { it.key == value }
        }
    }
}
