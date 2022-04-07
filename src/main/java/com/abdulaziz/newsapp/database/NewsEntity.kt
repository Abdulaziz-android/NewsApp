package com.abdulaziz.newsapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NewsEntity(
    @PrimaryKey(autoGenerate = true)
    var id:Int=0,
    var author:String,
    var title:String,
    var description:String,
    var text:String,
    var category:String,
    var date:String,
    var timestamp:Long,
    var image_url:String?,
    var view_count:Int
)
