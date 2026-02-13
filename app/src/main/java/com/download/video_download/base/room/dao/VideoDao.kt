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
    // 插入任务（冲突时替换）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Video)

    // 批量插入任务
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Video>)

    // 更新任务
    @Update
    suspend fun updateTask(task: Video)

    // 根据任务ID删除任务
    @Query("DELETE FROM video_table WHERE id = :taskId")
    suspend fun deleteTaskByTaskId(taskId: Long)

    // 根据URL删除任务
    @Query("DELETE FROM video_table WHERE url = :url")
    suspend fun deleteTaskByUrl(url: String)

    // 查询所有未完成的任务（排除已完成的）
    @Query("SELECT * FROM video_table WHERE downloadStatus != :completedStatus ORDER BY updateTime DESC")
    suspend fun getUncompletedTasks(completedStatus: Int = 2): List<Video> // Aria的STATE_COMPLETE=2

    // 查询所有任务
    @Query("SELECT * FROM video_table ORDER BY updateTime DESC")
    suspend fun getAllTasks(): List<Video>

    // 根据任务ID查询任务
    @Query("SELECT * FROM video_table WHERE id = :taskId LIMIT 1")
    suspend fun getTaskByTaskId(taskId: Long): Video?

    // 根据URL查询任务
    @Query("SELECT * FROM video_table WHERE url = :url LIMIT 1")
    suspend fun getTaskByUrl(url: String): Video?

    // 监听任务状态变化（Flow实时更新）
    @Query("SELECT * FROM video_table WHERE id = :taskId")
    fun observeTaskById(taskId: Long): Flow<Video?>
}