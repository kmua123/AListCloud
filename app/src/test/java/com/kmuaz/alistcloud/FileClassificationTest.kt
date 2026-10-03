package com.kmuaz.alistcloud

import com.kmuaz.alistcloud.data.network.model.FileItem
import com.kmuaz.alistcloud.data.repository.fullPath
import com.kmuaz.alistcloud.data.repository.validateZipPath
import com.kmuaz.alistcloud.ui.home.FileCategory
import org.junit.Assert.*
import org.junit.Test

class FileClassificationTest {
    @Test fun foldersAreExcludedAndCaseIsIgnored() {
        assertTrue(FileCategory.VIDEO.matches(FileItem("MOVIE.MP4", false, 10)))
        assertTrue(FileCategory.AUDIO.matches(FileItem("music.FLAC", false, 10)))
        assertTrue(FileCategory.IMAGE.matches(FileItem("photo.JPEG", false, 10)))
        assertTrue(FileCategory.DOCUMENT.matches(FileItem("report.PDF", false, 10)))
        assertTrue(FileCategory.ARCHIVE.matches(FileItem("backup.7Z", false, 10)))
        assertFalse(FileCategory.IMAGE.matches(FileItem("photos.jpg", true, 0)))
        assertFalse(FileCategory.VIDEO.matches(FileItem("notes.txt", false, 10)))
    }
    @Test fun sameNamedCategoryResultsKeepTheirOwnParent() {
        val first = FileItem("photo.jpg", false, 10, parent = "/drive-a/family")
        val second = first.copy(parent = "/drive-b/work")
        assertEquals("/drive-a/family/photo.jpg", first.fullPath("/"))
        assertEquals("/drive-b/work/photo.jpg", second.fullPath("/"))
        assertEquals("/photo.jpg", first.copy(parent = null).fullPath("/"))
    }
    @Test fun archivePathsCannotEscapeExtractionFolder() {
        assertEquals("nested/文件.txt", validateZipPath("nested/文件.txt"))
        listOf("../outside.txt", "/absolute.txt", "C:/file.txt", "nested/../../file.txt", "nested\\file.txt", "a//file.txt").forEach { path ->
            assertThrows(IllegalArgumentException::class.java) { validateZipPath(path) }
        }
    }
}
