package com.paulcraciunas.data.db.theme

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Themes", indices = [Index(value = ["theme"], unique = true)])
data class Theme(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "theme") val theme: String
)
