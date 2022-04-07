package com.abdulaziz.newsapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChannelEntity(
    @PrimaryKey
    val name:String
)
