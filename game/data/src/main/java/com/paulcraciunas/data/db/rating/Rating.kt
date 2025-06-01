package com.paulcraciunas.data.db.rating

import androidx.room.Entity
import androidx.room.PrimaryKey

// TODO Paul: is this useful?!
@Entity(tableName = "Ratings")
data class Rating(
    @PrimaryKey val value: Int // between 400 and 4000
)
