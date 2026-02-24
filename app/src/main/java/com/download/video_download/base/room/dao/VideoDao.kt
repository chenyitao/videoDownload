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
    suspend fun insertTask(task: Video)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Video>)

    @Update
    suspend fun updateTask(task: Video)

    @Query("DELETE FROM video_table WHERE id = :taskId")
    suspend fun deleteTaskByTaskId(taskId: Long)

    @Query("DELETE FROM video_table WHERE url = :url")
    suspend fun deleteTaskByUrl(url: String)

    @Query("SELECT * FROM video_table WHERE downloadStatus != :completedStatus ORDER BY updateTime DESC")
    suspend fun getUncompletedTasks(completedStatus: Int = 2): List<Video> // Aria的STATE_COMPLETE=2

    @Query("SELECT * FROM video_table ORDER BY updateTime DESC")
    suspend fun getAllTasks(): List<Video>

    @Query("SELECT * FROM video_table WHERE id = :taskId LIMIT 1")
    suspend fun getTaskByTaskId(taskId: Long): Video?

    @Query("SELECT * FROM video_table WHERE url = :url LIMIT 1")
    suspend fun getTaskByUrl(url: String): Video?

    @Query("SELECT * FROM video_table WHERE id = :taskId")
    fun observeTaskById(taskId: Long): Flow<Video?>
}