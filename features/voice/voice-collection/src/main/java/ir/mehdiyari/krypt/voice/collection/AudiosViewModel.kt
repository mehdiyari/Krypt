package ir.mehdiyari.krypt.voice.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.mehdiyari.krypt.files.logic.repositories.api.FilesRepository
import ir.mehdiyari.krypt.voice.shared.entity.AudioEntity
import ir.mehdiyari.krypt.voice.shared.entity.meta.AudioMetaDataJsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AudiosViewModel @Inject constructor(
    private val filesRepository: FilesRepository,
    private val audioEntityMapper: AudioEntityMapper,
    private val audioMetaDataJsonParser: AudioMetaDataJsonParser
) : ViewModel() {

    private val _audios = MutableStateFlow<List<AudioEntity>>(listOf())
    val audios = _audios.asStateFlow()

    fun getAudios() {
        viewModelScope.launch {
            if (filesRepository.getAudiosCount() != _audios.value.size.toLong()) {
                val size: Int
                _audios.value = filesRepository.getAllAudioFiles()
                    .sortedByDescending {
                        try {
                            audioMetaDataJsonParser.fromJson(it.metaData)?.date
                        } catch (t: Throwable) {
                            0L
                        }
                    }.map(audioEntityMapper::map).also {
                        size = it.size
                    }.mapIndexed { index, audioEntity ->
                        mapAudioName(index, audioEntity, size)
                    }
            }
        }
    }

    private fun mapAudioName(index: Int, audioEntity: AudioEntity, size: Int): AudioEntity =
        audioEntity.copy(name = "${audioEntity.name}${size - index}")
}