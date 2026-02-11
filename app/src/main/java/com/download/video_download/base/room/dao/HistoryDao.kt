package com.download.video_download.base.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.download.video_download.base.room.entity.History
import com.download.video_download.base.room.entity.Video
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: History)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorys(vararg historys: History)

    @Update
    suspend fun updateHistory(history: History)

    @Delete
    suspend fun deleteHistory(history: History)

    @Query("DELETE FROM history_table")
    suspend fun deleteAllHistory()

    @Query("SELECT * FROM history_table ORDER BY time DESC")
    suspend  fun getAllHistorys(): MutableList<History>

    @Query("SELECT COUNT(*) FROM history_table WHERE url = :url")
    suspend fun isUrlExists(url: String): Int

    @Query("SELECT COUNT(*) FROM history_table")
    suspend fun getHistoryCount(): Int

    @Query("DELETE FROM history_table WHERE time = (SELECT time FROM history_table ORDER BY time ASC LIMIT 1)")
    suspend fun deleteLastHistory()

//    @Query("SELECT * FROM video_table WHERE age > :minAge")
//    suspend fun getUsersOlderThan(minAge: Int): List<Video>
}