package com.download.video_download.base.room.entity

import androidx.media3.common.MimeTypes
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Entity(tableName = "video_table")
@TypeConverters(Converters::class)
@Serializable
data class Video(
    @PrimaryKey(autoGenerate = false)
    @SerialName("id")
    var id: Long = -1,
    @SerialName("url")
    var url: String = "",
    @SerialName("thumb")
    var thumb: String = "",
    @SerialName("speed")
    var speed: String = "",
    @SerialName("totalSize")
    var totalSize: Long = 0L,
    @SerialName("process")
    var process: Long = 0L,
    @SerialName("mimeTypes")
    var mimeTypes: String = "",
    @SerialName("path")
    var path: String = "",
    @SerialName("fileName")
    var fileName: String = "",
    @SerialName("downloadStatus")
    var downloadStatus: Int = -1,
    @SerialName("audioUrl")
    var audioUrl: String = "",
    @SerialName("duration")
    var duration: Long =  0L,
    @SerialName("isSelect")
    var isSelect: Boolean = false,
    @SerialName("audioTaskId")
    val audioTaskId: Long = -1L,
    @SerialName("downloadCompletedTime")
    var downloadCompletedTime: Long = 0L,
    @SerialName("downloadProcess")
    var downloadProcess: String = "",
    @SerialName("createTime")
    val createTime: Long = System.currentTimeMillis(),
    @SerialName("updateTime")
    val updateTime: Long = System.currentTimeMillis()
){
    fun getBaseName(): String {
        val title = this.fileName
        val sequentialSuffixRegex = Regex("\\(\\d+\\)$")
        val nameWithoutSuffix = sequentialSuffixRegex.replace(title, "").trim()
        val lastDotIndex = nameWithoutSuffix.lastIndexOf('.')
        return if (lastDotIndex != -1) {
            nameWithoutSuffix.take(lastDotIndex)
        } else {
            nameWithoutSuffix
        }
    }

}
class Converters {
    @TypeConverter
    fun fromLong(value: Long?): Long = value ?: 0L

    @TypeConverter
    fun fromString(value: String?): String = value ?: ""

    @TypeConverter
    fun fromDate(value: Date): Long = value.time

    @TypeConverter
    fun toDate(value: Long): Date = Date(value)
}
fun Video.getSequentialNumber(): Int {
    val title = this.fileName
    val pattern = Regex("\\((\\d+)\\)$")
    val matchResult = pattern.find(title)
    return matchResult?.groups?.get(1)?.value?.toInt() ?: 0
}