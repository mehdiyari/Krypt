package ir.mehdiyari.krypt.mediaList

import android.app.AlertDialog
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.glide.GlideImage
import ir.mehdiyari.krypt.core.designsystem.theme.KryptTheme
import ir.mehdiyari.krypt.mediaList.MediaViewAction.DECRYPT_MEDIA
import ir.mehdiyari.krypt.mediaList.MediaViewAction.ENCRYPT_MEDIA
import ir.mehdiyari.krypt.mediaList.MediaViewAction.PICK_MEDIA
import ir.mehdiyari.krypt.mediaList.MediaViewAction.SHARED_MEDIA
import ir.mehdiyari.krypt.shared.designsystem.resources.R as ResourcesR


@Composable
internal fun MediaScreenContent(
    selectedMediaItems: List<SelectedMediaItems>,
    actionState: MediaViewAction,
    notifyMediaScanner: Boolean,
    removeItemFromList: (String) -> Unit,
    deleteSelectedFromList: (String, Boolean) -> Unit,
    onNotifyChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    var showDeleteDialogForItem by remember { mutableStateOf<(Pair<String, Boolean>)?>(null) }

    Column(modifier = modifier) {
        if (actionState == DECRYPT_MEDIA) {
            NotifyMediaScannerCheckBox(
                notifyMediaScanner = notifyMediaScanner, onNotifyChanged = onNotifyChanged
            )
        }

        FileList(selectedMediaItems, removeItemFromList, onDeleteClicked = {
            val encrypted = actionState == DECRYPT_MEDIA
            showDeleteDialogForItem = it to encrypted
        }, modifier = modifier.padding(8.dp))

        if (showDeleteDialogForItem != null) {
            ConfirmDeleteFileDialog(onDismiss = {
                showDeleteDialogForItem = null
            }, onConfirmClicked = {
                deleteSelectedFromList(
                    showDeleteDialogForItem!!.first,
                    showDeleteDialogForItem!!.second
                )
                showDeleteDialogForItem = null
            }, descriptionRes = R.string.delete_selected_file)
        }

    }
}

@Composable
private fun ConfirmDeleteFileDialog(
    onDismiss: () -> Unit, onConfirmClicked: () -> Unit, @StringRes descriptionRes: Int
) {
    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(text = stringResource(id = R.string.delete_file))
    }, text = {
        Text(
            modifier = Modifier.padding(bottom = 10.dp), text = stringResource(id = descriptionRes)
        )
    }, confirmButton = {
        OutlinedButton(
            onClick = onConfirmClicked,
        ) {
            Text(stringResource(id = ir.mehdiyari.krypt.shared.designsystem.resources.R.string.YES))
        }

    }, dismissButton = {
        OutlinedButton(onClick = onDismiss) {
            Text(stringResource(id = ir.mehdiyari.krypt.shared.designsystem.resources.R.string.NO))
        }
    })
}

@Composable
internal fun ShowActionButton(
    viewState: MediaViewState,
    actionState: MediaViewAction,
    notifyMediaScanner: Boolean,
    deleteAllSelectedFiles: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    if (viewState is MediaViewState.EncryptDecryptState) {
        val buttonTextAndDeleteText: Pair<String, String> = when (actionState) {
            PICK_MEDIA, ENCRYPT_MEDIA, SHARED_MEDIA -> {
                (stringResource(id = R.string.encrypt_action) to stringResource(id = R.string.delete_files_after_encrypt_dialog_message))
            }

            DECRYPT_MEDIA -> {
                stringResource(id = R.string.decrypt_action) to stringResource(id = R.string.delete_files_after_decrypt_dialog_message)
            }

            else -> {
                "" to ""
            }
        }
        val encryptDecryptState = viewState as? MediaViewState.EncryptDecryptState
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End,
            modifier = modifier.padding(20.dp)
        ) {
            ActionFloatingButton(buttonTextAndDeleteText.first) {
                AlertDialog.Builder(context)
                    .setMessage(buttonTextAndDeleteText.second)
                    .setPositiveButton(context.getString(ir.mehdiyari.krypt.shared.designsystem.resources.R.string.YES)) { d, _ ->
                        d.dismiss()
                        encryptDecryptState?.onEncryptOrDecryptAction?.invoke(
                            true, notifyMediaScanner
                        )
                    }
                    .setNegativeButton(context.getString(ir.mehdiyari.krypt.shared.designsystem.resources.R.string.NO)) { d, _ ->
                        d.dismiss()
                        encryptDecryptState?.onEncryptOrDecryptAction?.invoke(
                            false, notifyMediaScanner
                        )
                    }.create().show()
            }
        }

        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start,
            modifier = modifier.padding(20.dp)
        ) {
            DeleteAllFilesFloatingButton(deleteAllSelectedFiles = deleteAllSelectedFiles)
        }
    }
}


@Composable
internal fun OperationResult(
    @DrawableRes imageRes: Int, @StringRes messageRes: Int, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(imageRes),
            contentDescription = stringResource(id = messageRes),
            modifier = Modifier.size(100.dp)
        )
        Text(
            text = stringResource(id = messageRes),
        )
    }
}

@Composable
internal fun OperationStart(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(id = R.string.loading),
        )
    }
}

@Composable
private fun NotifyMediaScannerCheckBox(
    notifyMediaScanner: Boolean, onNotifyChanged: (Boolean) -> Unit
) {
    Row(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 0.dp)) {
        Checkbox(checked = notifyMediaScanner, onCheckedChange = {
            onNotifyChanged(it)
        })

        Text(
            text = stringResource(id = R.string.notify_media_scanner),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .selectable(false, onClick = {
                    onNotifyChanged(!notifyMediaScanner)
                })
                .align(Alignment.CenterVertically)
        )
    }
}

@Composable
@Preview
private fun ActionFloatingButton(
    buttonText: String = stringResource(id = R.string.encrypt_action),
    onButtonClick: () -> Unit = {}
) {
    ExtendedFloatingActionButton(
        onClick = {
            onButtonClick()
        },
        contentColor = MaterialTheme.colorScheme.onPrimary,
        text = { Text(text = buttonText) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_encrypt_decrypt_action),
                contentDescription = buttonText
            )
        },
        containerColor = MaterialTheme.colorScheme.primary
    )
}


@Composable
private fun DeleteAllFilesFloatingButton(
    deleteAllSelectedFiles: () -> Unit
) {
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    ExtendedFloatingActionButton(
        onClick = {
            showConfirmDeleteDialog = true
        },
        text = { Text(text = stringResource(id = R.string.delete_all)) },
        icon = {
            Icon(
                Icons.Filled.Delete, contentDescription = stringResource(id = R.string.delete_all)
            )
        },
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    )

    if (showConfirmDeleteDialog) {
        ConfirmDeleteFileDialog(onDismiss = {
            showConfirmDeleteDialog = false
        }, onConfirmClicked = {
            deleteAllSelectedFiles()
        }, descriptionRes = R.string.delete_all_selected_files
        )
    }
}

@Composable
private fun FileList(
    selectedMediaItems: List<SelectedMediaItems>,
    onRemoveClicked: (String) -> Unit,
    onDeleteClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(selectedMediaItems) { selectedMediaItem ->
            FileItem(
                item = selectedMediaItem,
                onRemoveClicked = { onRemoveClicked(selectedMediaItem.path) },
                onDeleteClicked = { onDeleteClicked(selectedMediaItem.path) },
            )
        }
    }
}

@Composable
private fun FileItem(
    item: SelectedMediaItems,
    onRemoveClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Row(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)) {
            if (item.path.isNotBlank()) {
                GlideImage(
                    imageModel = {
                        item.path
                    },
                    requestOptions = {
                        RequestOptions().override(80, 80).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Image(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentDescription = "",
                    painter = painterResource(id = ResourcesR.drawable.ic_gallery_50),
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = item.getFileRealName(), fontSize = 16.sp, maxLines = 2
                )

                Text(
                    text = item.getFileSize(), fontSize = 14.sp, maxLines = 1
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 8.dp, end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                modifier = Modifier
                    .clickable(onClick = {
                        onDeleteClicked()
                    }, role = Role.Button)
                    .padding(start = 4.dp, end = 4.dp)
                    .size(24.dp)
            )

            Icon(
                painter = painterResource(R.drawable.ic_remove),
                contentDescription = null,
                modifier = Modifier
                    .clickable(onClick = {
                        onRemoveClicked()
                    }, role = Role.Button)
                    .padding(start = 4.dp, end = 4.dp)
                    .size(24.dp)
            )
        }

    }
}

@Preview
@Composable
private fun OperationStartViewPreview() {
    KryptTheme {
        Surface {
            OperationStart()
        }
    }
}

@Preview
@Composable
private fun OperationSuccessPreview() {
    KryptTheme {
        Surface {
            OperationResult(
                imageRes = R.drawable.operation_done, messageRes = R.string.operation_successfully
            )
        }
    }
}

@Preview
@Composable
private fun OperationFailedPreview() {
    KryptTheme {
        Surface {
            OperationResult(
                imageRes = R.drawable.operation_failed, messageRes = R.string.operation_failed
            )
        }
    }
}

@Preview
@Composable
private fun FileItemPreview(@PreviewParameter(SelectedMediaItemsPreviewParameterProvider::class) items: List<SelectedMediaItems>) {
    KryptTheme {
        Surface {
            FileItem(
                item = items[0],
                onRemoveClicked = {},
                onDeleteClicked = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun FileListPreview(@PreviewParameter(SelectedMediaItemsPreviewParameterProvider::class) items: List<SelectedMediaItems>) {
    KryptTheme {
        Surface {
            FileList(
                selectedMediaItems = items,
                onRemoveClicked = {},
                onDeleteClicked = {},
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun ConfirmDeletePreview() {
    KryptTheme {
        Surface {
            ConfirmDeleteFileDialog(
                onDismiss = { /*TODO*/ },
                onConfirmClicked = { /*TODO*/ },
                descriptionRes = R.string.delete_all_selected_files
            )
        }
    }
}

private class SelectedMediaItemsPreviewParameterProvider :
    PreviewParameterProvider<List<SelectedMediaItems>> {
    override val values: Sequence<List<SelectedMediaItems>>
        get() = sequenceOf(List(5) { SelectedMediaItems("", false) })

}