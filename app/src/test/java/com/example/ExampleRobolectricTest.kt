package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DownloadEntity
import com.example.model.DownloadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("toDus Download", appName)
    }

    @Test
    fun `test download entity formatting and properties`() {
        val entity = DownloadEntity(
            id = 1,
            url = "https://files.todus.cu/data/app_update.apk",
            fileName = "app_update.apk",
            fileSize = 10485760L, // 10 MB
            downloadedBytes = 5242880L, // 5 MB
            status = DownloadStatus.DOWNLOADING,
            speedBytesPerSec = 1048576L, // 1 MB/s
            threadsCount = 16
        )

        assertEquals("apk", entity.fileExtension)
        assertTrue(entity.isApk)
        assertEquals(50, entity.progressPercent)
        assertEquals("10.0 MB", entity.formattedSize)
        assertEquals("5.0 MB", entity.formattedDownloaded)
        assertEquals("1.0 MB/s", entity.formattedSpeed)
        assertEquals(5L, entity.etaSeconds)
        assertEquals("5s", entity.formattedEta)
    }
}
