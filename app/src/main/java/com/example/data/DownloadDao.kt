package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.DownloadEntity
import com.example.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status != 'COMPLETED' ORDER BY createdAt DESC")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    fun getDownloadById(id: Long): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadByIdOnce(id: Long): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity): Long

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET status = :status, speedBytesPerSec = :speed WHERE id = :id")
    suspend fun updateStatusAndSpeed(id: Long, status: DownloadStatus, speed: Long)

    @Query("UPDATE downloads SET downloadedBytes = :downloaded, speedBytesPerSec = :speed WHERE id = :id")
    suspend fun updateProgress(id: Long, downloaded: Long, speed: Long)

    @Query("UPDATE downloads SET status = 'COMPLETED', downloadedBytes = fileSize, completedAt = :completedAt, speedBytesPerSec = 0 WHERE id = :id")
    suspend fun markCompleted(id: Long, completedAt: Long)

    @Query("UPDATE downloads SET status = 'FAILED', errorMsg = :error, speedBytesPerSec = 0 WHERE id = :id")
    suspend fun markFailed(id: Long, error: String)

    @Query("UPDATE downloads SET status = 'RECONNECTING', reconnectAttempt = :attempt, speedBytesPerSec = 0 WHERE id = :id")
    suspend fun markReconnecting(id: Long, attempt: Int)

    @Query("UPDATE downloads SET status = 'PAUSED', speedBytesPerSec = 0 WHERE id = :id")
    suspend fun markPaused(id: Long)

    @Query("UPDATE downloads SET status = 'PAUSED', speedBytesPerSec = 0 WHERE status = 'DOWNLOADING' OR status = 'RECONNECTING'")
    suspend fun pauseAllActive()

    @Delete
    suspend fun deleteDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()
}
