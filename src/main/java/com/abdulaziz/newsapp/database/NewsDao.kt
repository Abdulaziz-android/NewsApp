package com.abdulaziz.newsapp.database

import androidx.room.*

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNews(newsEntity: NewsEntity)

    @Delete
    fun deleteNews(newsEntity: NewsEntity)

    @Query("select * from newsentity")
    fun getAllNews(): List<NewsEntity>

    @Query("SELECT * FROM newsentity ORDER BY id DESC LIMIT 1")
    fun getLastNews(): NewsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannel(channelEntity: ChannelEntity)

    @Delete
    fun deleteChannel(channelEntity: ChannelEntity)

    @Query("select * from channelentity")
    fun getAllChannels(): List<ChannelEntity>

}