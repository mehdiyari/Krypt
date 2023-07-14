package ir.mehdiyari.krypt.data.repositories.files

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import ir.mehdiyari.krypt.app.user.UsernameProvider
import ir.mehdiyari.krypt.data.backup.BackupDao
import ir.mehdiyari.krypt.data.file.FileEntity
import ir.mehdiyari.krypt.data.file.FileTypeEnum
import ir.mehdiyari.krypt.data.file.FilesDao
import ir.mehdiyari.krypt.utils.FilesUtilities
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FilesRepositoryTest {

    private val ioDispatcher = UnconfinedTestDispatcher()
    private lateinit var filesDao: FilesDao
    private lateinit var backupDao: BackupDao
    private lateinit var usernameProvider: UsernameProvider
    private lateinit var filesUtilities: FilesUtilities
    private lateinit var filesRepository: FilesRepository
    private lateinit var fileWrapper: DefaultFilesRepository.FileWrapper

    @Before
    fun setup() {
        filesDao = mockk<FilesDao>()
        backupDao = mockk<BackupDao>()
        usernameProvider = mockk<UsernameProvider>()
        filesUtilities = mockk<FilesUtilities>()
        fileWrapper = mockk()
        filesRepository = DefaultFilesRepository(
            filesDao,
            backupDao,
            usernameProvider,
            filesUtilities,
            fileWrapper,
            ioDispatcher
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    @Test
    fun `getAllFilesTypeCounts should return correct counts`() = runTest {
        val username = "testUser"
        every { usernameProvider.getUsername() } returns username
        coEvery { filesDao.getFilesCountBasedOnType(username, any()) } returns 1

        val result = filesRepository.getAllFilesTypeCounts()

        assertEquals(FileTypeEnum.values().size, result.size)
        assertTrue(result.all { it.second == 1L })
    }

    @Test
    fun `getAllFilesTypeCounts should return 0 if usernameProvider#getUsername() is null`() =
        runTest {
            val username: String? = null
            every { usernameProvider.getUsername() } returns username
            coEvery { filesDao.getFilesCountBasedOnType("username", any()) } returns 1

            val result = filesRepository.getAllFilesTypeCounts()

            assertEquals(FileTypeEnum.values().size, result.size)
            assertTrue(result.all { it.second == 0L })
        }

    @Test
    fun `insertFiles should correctly insert files`() = runTest {
        val username = "testUser"
        val files = listOf(
            FileEntity(
                id = 1,
                type = null,
                filePath = "something",
                metaData = "meta",
                accountName = "user"
            )
        )
        every { usernameProvider.getUsername() } returns username
        coEvery { filesDao.insertFiles(any()) } just Runs

        filesRepository.insertFiles(files)

        coVerify { filesDao.insertFiles(files.map { it.copy(accountName = username) }) }
    }

    @Test
    fun `getMediasCount should return correct count`() = runTest {
        val username = "testUser"
        val photoCount = 3L
        val videoCount = 2L
        every { usernameProvider.getUsername() } returns username
        coEvery {
            filesDao.getFilesCountBasedOnType(
                username,
                FileTypeEnum.Photo
            )
        } returns photoCount
        coEvery {
            filesDao.getFilesCountBasedOnType(
                username,
                FileTypeEnum.Video
            )
        } returns videoCount

        val result = filesRepository.getMediasCount()

        assertEquals(photoCount + videoCount, result)
    }

    @Test
    fun `mapThumbnailsAndNameToFileEntity should return correct FileEntity list`() = runTest {
        val username = "testUsername"
        val medias = arrayOf("media1", "/path/media2")
        val nameOfFile = "media2"
        val allEncryptedMedia = generateFileEntity()

        coEvery { usernameProvider.getUsername() } returns username
        coEvery { filesDao.getAllMedia(username) } returns allEncryptedMedia
        coEvery { filesUtilities.getNameOfFileWithExtension("/path/media2") } returns nameOfFile

        val result = filesRepository.mapThumbnailsAndNameToFileEntity(medias)

        assertEquals(3, result.size)
        assertEquals("media1", result[0].filePath)
        assertEquals("/path/media2", result[1].filePath)
        assertEquals("media2", result[2].metaData)
    }

    @Test
    fun `deleteEncryptedFilesFromKryptDBAndFileSystem should delete all given files`() = runTest {
        val files = generateFileEntity()

        coEvery { filesDao.deleteFiles(any()) } just runs
        coEvery { fileWrapper.delete(any()) } returns true

        filesRepository.deleteEncryptedFilesFromKryptDBAndFileSystem(files)

        coVerify { filesDao.deleteFiles(files) }
        coVerify { fileWrapper.delete("media1") }
        coVerify { fileWrapper.delete("/path/media2") }
        coVerify { fileWrapper.delete("media3") }
        coVerify { fileWrapper.delete("media") }
    }

    @Test
    fun `getAllFilesSize should return total size of all files`() = runTest {
        val username = "test_user"
        val backupFiles = listOf("backup_file1", "backup_file2")
        val daoFiles = listOf("dao_file1", "dao_file2")
        val backupFilesSize = 100L
        val daoFilesSize = 200L
        coEvery { backupDao.getAllBackupFiles(username) } returns backupFiles
        coEvery { filesDao.getAllFilesPath(username) } returns daoFiles
        coEvery { fileWrapper.length(any()) } returnsMany listOf(
            backupFilesSize,
            backupFilesSize,
            daoFilesSize,
            daoFilesSize
        )
        every { usernameProvider.getUsername() } returns username

        val totalSize = filesRepository.getAllFilesSize()

        coVerify { backupDao.getAllBackupFiles(username) }
        coVerify { filesDao.getAllFilesPath(username) }
        coVerify { fileWrapper.length("backup_file1") }
        coVerify { fileWrapper.length("backup_file2") }
        coVerify { fileWrapper.length("dao_file1") }
        coVerify { fileWrapper.length("dao_file2") }
        assertEquals((backupFilesSize * backupFiles.size + daoFilesSize * daoFiles.size), totalSize)
    }

    private fun generateFileEntity() = listOf(
        FileEntity(
            filePath = "media1",
            accountName = "",
            metaData = "",
            type = FileTypeEnum.Photo
        ),
        FileEntity(
            filePath = "/path/media2",
            accountName = "",
            metaData = "",
            type = FileTypeEnum.Photo
        ),
        FileEntity(
            filePath = "media3",
            accountName = "",
            metaData = "",
            type = FileTypeEnum.Photo
        ),
        FileEntity(
            filePath = "media",
            accountName = "",
            metaData = "media2",
            type = FileTypeEnum.Photo
        )
    )

}