package com.abdulaziz.newsapp.models

import java.io.Serializable

class News:Serializable{

    var author:String?=null
    var title:String?=null
    var description:String?=null
    var text:String?=null
    var category:String?=null
    var date:String?=null
    var timestamp:Long?=null
    var image_url:String?=null
    var view_count:Int?=null

    constructor()

    constructor(
        author: String?,
        title: String?,
        description: String?,
        text: String?,
        category: String?,
        date: String?,
        timestamp: Long?,
        image_url: String?,
        view_count: Int?
    ) {
        this.author = author
        this.title = title
        this.description = description
        this.text = text
        this.category = category
        this.date = date
        this.timestamp = timestamp
        this.image_url = image_url
        this.view_count = view_count
    }


}
