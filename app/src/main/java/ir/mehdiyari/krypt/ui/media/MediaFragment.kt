package ir.mehdiyari.krypt.ui.media

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ir.mehdiyari.fallery.main.fallery.*
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.krypt.R
import ir.mehdiyari.krypt.app.AppLockerStopApi
import ir.mehdiyari.krypt.ui.media.player.PlayerActivity
import ir.mehdiyari.krypt.ui.media.player.addExtraForPlayerToIntent
import ir.mehdiyari.krypt.utils.DeviceGalleryImageLoader
import ir.mehdiyari.krypt.utils.getFileProviderAuthority
import ir.mehdiyari.krypt.utils.isInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MediaFragment : Fragment() {

    private val viewModel: MediasViewModel by viewModels()
    private val args: MediaFragmentArgs by navArgs()

    @field:Inject
    lateinit var deviceGalleryImageLoader: DeviceGalleryImageLoader

    @field:Inject
    lateinit var encryptedMediasBucketProvider: EncryptedMediasBucketProvider

    @field:Inject
    lateinit var encryptedMediasBucketContentProvider: EncryptedMediasBucketContentProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewAction.collect {
                if (viewModel.selectedMediasFlow.value.isEmpty()) {
                    when (it) {
                        MediaFragmentAction.PICK_MEDIA -> {
                            openMediaPicker()
                        }
                        MediaFragmentAction.DECRYPT_MEDIA -> {

                            if (viewModel.checkForOpenPickerForDecryptMode()) {
                                openMediaPickerForDecrypting()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.no_encrypted_file_found,
                                    Toast.LENGTH_LONG
                                ).show()
                                findNavController().popBackStack()
                            }

                        }
                        MediaFragmentAction.ENCRYPT_MEDIA -> handleEncryptSharedMedia()
                        MediaFragmentAction.TAKE_MEDIA -> TODO()
                        MediaFragmentAction.DEFAULT -> {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messageFlow.collect {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleEncryptSharedMedia() {
        if (args.sharedMedias == null || args.sharedMedias?.images?.isEmpty() == true) {
            Toast.makeText(requireContext(), R.string.shared_media_not_found, Toast.LENGTH_SHORT)
                .show()
            findNavController().popBackStack()
        } else {
            viewModel.onDecryptSharedMedia(args.sharedMedias?.images)
        }
    }

    private fun getBaseOptionsOfFallery(): FalleryBuilder = FalleryBuilder()
        .setImageLoader(deviceGalleryImageLoader)
        .mediaTypeFiltering(BucketType.VIDEO_PHOTO_BUCKETS)
        .setFalleryToolbarTitleText(R.string.app_name)
        .setMediaCountEnabled(true)
        .setGrantExternalStoragePermission(true)
        .setGrantSharedStoragePermission(true)
        .setMediaObserverEnabled(true)
        .setCaptionEnabledOptions(CaptionEnabledOptions(false))
        .setFallerySpanCountMode(FalleryBucketsSpanCountMode.UserZoomInOrZoomOut)
        .setTheme(if (requireContext().isInDarkTheme()) ir.mehdiyari.fallery.R.style.Fallery_Dracula else ir.mehdiyari.fallery.R.style.Fallery_Light)

    private fun openMediaPickerForDecrypting() {
        getBaseOptionsOfFallery()
            .setContentProviders(
                encryptedMediasBucketContentProvider,
                encryptedMediasBucketProvider
            ).setOnVideoPlayClick {
                startActivity(
                    Intent(requireContext(), PlayerActivity::class.java).addExtraForPlayerToIntent(
                        it, true
                    )
                )
            }
            .build().also { options ->
                startFalleryWithOptions(2, options)
                handleAutoLockBeforeStartMediaPicker()
            }
    }

    private fun handleAutoLockBeforeStartMediaPicker() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            try {
                (requireActivity() as AppLockerStopApi).stopAppLockerManually()
            } catch (t: java.lang.ClassCastException) {
                t.printStackTrace()
            }
        }
    }

    private fun openMediaPicker() {
        getBaseOptionsOfFallery().setCameraEnabledOptions(
            CameraEnabledOptions(
                true,
                getFileProviderAuthority(requireActivity().application.packageName)
            )
        ).setOnVideoPlayClick {
            startActivity(
                Intent(requireContext(), PlayerActivity::class.java).addExtraForPlayerToIntent(
                    it, false
                )
            )
        }.build().also { options ->
            startFalleryWithOptions(1, options)
            handleAutoLockBeforeStartMediaPicker()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 || requestCode == 2) {
            if (resultCode == RESULT_OK) {
                handleSelectedMedia(data?.getFalleryResultMediasFromIntent())
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private fun handleSelectedMedia(result: Array<String>?) {
        if (result.isNullOrEmpty()) {
            findNavController().popBackStack()
        } else {
            viewModel.onSelectedMedias(result)
        }
    }
}