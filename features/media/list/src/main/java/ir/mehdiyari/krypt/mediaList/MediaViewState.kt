package ir.mehdiyari.krypt.mediaList

internal sealed class MediaViewState {

    data class EncryptDecryptState(
        val selectedMediaItems: List<SelectedMediaItems>,
        val onEncryptOrDecryptAction: (deleteAfterEncryption: Boolean, notifyMediaScanner: Boolean) -> Unit,
    ) : MediaViewState()

    object Default : MediaViewState()

    object OperationStart : MediaViewState()
    object OperationFinished : MediaViewState()
    object OperationFailed : MediaViewState()
}