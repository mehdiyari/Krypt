package ir.mehdiyari.krypt.files.logic.repositories.impl

import ir.mehdiyari.krypt.account.api.UsernameProvider
import ir.mehdiyari.krypt.backup.data.dao.BackupDao
import ir.mehdiyari.krypt.dispatchers.di.DispatchersQualifierType
import ir.mehdiyari.krypt.dispatchers.di.DispatchersType
import ir.mehdiyari.krypt.file.data.dao.FilesDao
import ir.mehdiyari.krypt.file.data.entity.FileEntity
import ir.mehdiyari.krypt.file.data.entity.FileTypeEnum
import ir.mehdiyari.krypt.files.logic.repositories.api.FilesRepository
import ir.mehdiyari.krypt.files.logic.utils.FileWrapper
import ir.mehdiyari.krypt.files.logic.utils.FilesUtilities
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FilesRepositoryImpl @Inject constructor(
    private val filedDao: FilesDao,
    private val backupDao: BackupDao,
    private val usernameProvider: UsernameProvider,
    private val filesUtilities: FilesUtilities,
    private val fileWrapper: FileWrapper,
    @DispatchersType(DispatchersQualifierType.IO) private val ioDispatcher: CoroutineDispatcher
) : FilesRepository {

    override suspend fun getAllFilesTypeCounts(): List<Pair<FileTypeEnum, Long>> =
        mutableListOf<Pair<FileTypeEnum, Long>>().apply {
            FileTypeEnum.values().forEach { fileType ->
                add(
                    fileType to try {
                        filedDao.getFilesCountBasedOnType(
                            usernameProvider.getUsername()!!,
                            fileType
                        )
                    } catch (t: Throwable) {
                        0
                    }
                )
            }
        }.toList()


    override suspend fun insertFiles(
        files: List<FileEntity>
    ) {
        filedDao.insertFiles(files.map {
            it.copy(accountName = usernameProvider.getUsername()!!)
        })
    }

    override suspend fun getMediasCount(): Long = filedDao.getFilesCountBasedOnType(
        usernameProvider.getUsername()!!,
        FileTypeEnum.Photo
    ) + filedDao.getFilesCountBasedOnType(
        usernameProvider.getUsername()!!,
        FileTypeEnum.Video
    )

    override suspend fun getLastEncryptedMediaThumbnail(): String? =
        internalGetLastThumb(FileTypeEnum.Photo, FileTypeEnum.Video)

    override suspend fun getLastEncryptedPhotoThumbnail(): String? =
        internalGetLastThumb(FileTypeEnum.Photo)

    override suspend fun getLastEncryptedVideoThumbnail(): String? =
        internalGetLastThumb(FileTypeEnum.Video)

    private suspend fun internalGetLastThumb(
        vararg types: FileTypeEnum
    ): String? = filedDao.getAllFilesOfCurrentAccountBasedOnType(
        usernameProvider.getUsername()!!,
        *types
    ).lastOrNull {
        it.metaData.isNotBlank()
    }?.metaData

    override suspend fun getAllEncryptedMedia(): List<FileEntity> =
        filedDao.getAllMedia(
            usernameProvider.getUsername()!!
        )

    override suspend fun mapThumbnailsAndNameToFileEntity(medias: Array<String>): List<FileEntity> =
        mutableListOf<FileEntity>().apply {
            getAllEncryptedMedia().filter {
                medias.any { currentMedia ->
                    if (!currentMedia.contains("/")) {
                        it.filePath.contains(currentMedia)
                    } else {
                        val nameOfFile = filesUtilities.getNameOfFileWithExtension(currentMedia)
                        it.metaData.contains(nameOfFile) || it.filePath.contains(nameOfFile)
                    }
                }
            }.also(this::addAll)
        }

    override suspend fun deleteEncryptedFilesFromKryptDBAndFileSystem(files: List<FileEntity>) {
        filedDao.deleteFiles(files)
        files.forEach {
            fileWrapper.delete(it.filePath)
            if (
                it.metaData.isNotBlank()
                && (it.type == FileTypeEnum.Photo || it.type == FileTypeEnum.Video)
            ) {
                fileWrapper.delete(it.metaData)
            }
        }
    }

    override suspend fun getAllTextFiles(): List<FileEntity> =
        filedDao.getAllFilesOfCurrentAccountBasedOnType(
            usernameProvider.getUsername()!!,
            FileTypeEnum.Text
        )

    override suspend fun getFileById(id: Long): FileEntity? =
        filedDao.getFileById(usernameProvider.getUsername()!!, id)

    override suspend fun getAllFiles(): List<FileEntity> =
        filedDao.getAllFiles(usernameProvider.getUsername()!!)

    override suspend fun getAllFilesSize(): Long {
        var total = 0L
        (try {
            mutableListOf<String>().apply {
                addAll(backupDao.getAllBackupFiles(usernameProvider.getUsername()!!) ?: listOf())
                addAll(filedDao.getAllFilesPath(usernameProvider.getUsername()!!) ?: listOf())
            }
        } catch (t: Throwable) {
            null
        })?.map {
            fileWrapper.length(it)
        }?.forEach {
            total += it
        }

        return total
    }

    override suspend fun getAllImages(): List<FileEntity> = filedDao.getAllMedia(
        usernameProvider.getUsername()!!, listOf(FileTypeEnum.Photo)
    )

    override suspend fun getAllVideos(): List<FileEntity> = filedDao.getAllMedia(
        usernameProvider.getUsername()!!, listOf(FileTypeEnum.Video)
    )

    override suspend fun getPhotosCount(): Long = filedDao.getFilesCountBasedOnType(
        usernameProvider.getUsername()!!,
        FileTypeEnum.Photo
    )

    override suspend fun getAudiosCount(): Long = withContext(ioDispatcher) {
        filedDao.getFilesCountBasedOnType(
            usernameProvider.getUsername()!!,
            FileTypeEnum.Audio
        )
    }

    override suspend fun getVideosCount(): Long = filedDao.getFilesCountBasedOnType(
        usernameProvider.getUsername()!!,
        FileTypeEnum.Video
    )

    override suspend fun getFileByThumbPath(thumbFileName: String): FileEntity? =
        filedDao.getMediaFileByPath(
            usernameProvider.getUsername()!!, thumbFileName
        )

    override suspend fun getAllAudioFiles(): List<FileEntity> = withContext(ioDispatcher) {
        filedDao.getAllFilesOfCurrentAccountBasedOnType(
            usernameProvider.getUsername()!!,
            FileTypeEnum.Audio
        )
    }

    override suspend fun updateFile(fileEntity: FileEntity): Unit = withContext(ioDispatcher) {
        filedDao.updateFile(fileEntity)
    }

    override suspend fun getAudioById(id: Long): FileEntity? = withContext(ioDispatcher) {
        filedDao.getFileById(usernameProvider.getUsername()!!, id)
    }
}