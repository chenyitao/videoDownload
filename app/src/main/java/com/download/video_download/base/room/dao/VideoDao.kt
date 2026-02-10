package com.download.video_download.base.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.download.video_download.base.room.entity.Video
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: Video)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(vararg videos: Video)

    @Update
    suspend fun updateVideo(user: Video)

    @Delete
    suspend fun deleteVideo(user: Video)

    @Query("DELETE FROM video_table")
    suspend fun deleteAllVideo()

    @Query("SELECT * FROM video_table ORDER BY id ASC")
    fun getAllVideos(): Flow<List<Video>>

//    @Query("SELECT * FROM video_table WHERE age > :minAge")
//    suspend fun getUsersOlderThan(minAge: Int): List<Video>
}