package ir.mehdiyari.krypt.backup

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.documentfile.provider.DocumentFile
import ir.mehdiyari.krypt.core.designsystem.theme.KryptTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ir.mehdiyari.krypt.shared.designsystem.resources.R as DesignSystemResourceR


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun DataScreenScaffold(
    modifier: Modifier,
    onNavigationClicked: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
    content: @Composable () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = DesignSystemResourceR.string.menu_data_usage),
                        textAlign = TextAlign.Start
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigationClicked()
                        },
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack, "",
                            modifier = modifier.padding(start = 4.dp)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

@Composable
fun FileSizeView(modifier: Modifier, fileSizeState: State<String>) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.krypt_file_sizes),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = fileSizeState.value,
            textAlign = TextAlign.Center,
            fontSize = 25.sp,
            modifier = modifier.padding(top = 8.dp)
        )

        DataBaseDivider(modifier)
    }
}

@Composable
internal fun BackupView(
    modifier: Modifier,
    lastBackupState: State<String?>,
    backupState: State<BackupViewState?>,
    backupNowClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        ConstraintLayout(
            modifier = modifier.fillMaxWidth()
        ) {
            val (title, dateTime) = createRefs()

            Text(
                text = stringResource(id = R.string.last_backup_date),
                fontWeight = FontWeight.Bold,
                modifier = modifier.constrainAs(title) {
                    start.linkTo(parent.start, margin = 20.dp)
                }
            )

            val dateTimeString = if (lastBackupState.value.isNullOrBlank()) {
                stringResource(id = R.string.no_backup_yet)
            } else {
                lastBackupState.value ?: ""
            }

            Text(
                text = dateTimeString,
                fontWeight = FontWeight.Bold,
                modifier = modifier.constrainAs(dateTime) {
                    end.linkTo(parent.end, margin = 20.dp)
                }
            )
        }

        val stateValue = backupState.value
        if (stateValue is BackupViewState.Started) {
            CircularProgressIndicator(
                modifier = modifier.padding(top = 8.dp, start = 20.dp, end = 20.dp)
            )
        } else {
            val colorIfError = if (stateValue is BackupViewState.Failed) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }

            Button(
                modifier = modifier.padding(top = 8.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorIfError
                ),
                onClick = {
                    backupNowClick()
                }
            ) {
                when (stateValue) {
                    is BackupViewState.Failed -> {
                        Text(text = stringResource(id = R.string.retry_backup))
                    }

                    else -> {
                        Text(text = stringResource(id = R.string.backup))
                    }
                }
            }
        }

        DataBaseDivider(modifier)
    }
}

@Composable
internal fun DataBaseDivider(modifier: Modifier) {
    Divider(
        modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
internal fun DeleteBackupFileDialog(
    modifier: Modifier,
    deleteDialogState: MutableState<Pair<Boolean, Int>> = mutableStateOf(true to -1),
    onDeleteBackUp: (Int) -> Unit,
) {
    if (deleteDialogState.value.first) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = {
                deleteDialogState.value = false to -1
            },
            title = {
                Text(text = stringResource(id = R.string.delete_backup_file_title))
            },
            text = {
                Text(
                    modifier = modifier.padding(bottom = 10.dp),
                    text = stringResource(id = R.string.delete_backup_file)
                )
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        onDeleteBackUp(deleteDialogState.value.second)
                        deleteDialogState.value = false to -1
                    },
                ) {
                    Text(stringResource(id = ir.mehdiyari.krypt.shared.designsystem.resources.R.string.YES))
                }

            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        deleteDialogState.value = false to -1
                    }
                ) {
                    Text(stringResource(id = ir.mehdiyari.krypt.shared.designsystem.resources.R.string.NO))
                }
            }
        )
    }
}

@Composable
internal fun ChooseDirectoryView(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onSelectDirectory: (uri: Uri) -> Unit
) {
    val chooseDirectorySnackbarMsg = stringResource(id = R.string.choose_backup_directory_description)
    val chooseDirectorySnackbarAction = stringResource(id = R.string.ok)
    val permissionErrorSnackbarMsg = stringResource(id = R.string.file_and_media_permisson_error)
    val context = LocalContext.current

    var selectedDirectory: DocumentFile? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    selectedDirectory = DocumentFile.fromTreeUri(
                        context, it
                    )
                }
            }
        }
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            launcher.launch(intent)
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = permissionErrorSnackbarMsg,
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite
                )
            }
        }
    }
    LaunchedEffect(true) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = chooseDirectorySnackbarMsg,
                actionLabel = chooseDirectorySnackbarAction,
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            ).apply {
                if (this == SnackbarResult.ActionPerformed) {
                    val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                    if (ContextCompat.checkSelfPermission(
                            context,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(permission)
                    } else {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        launcher.launch(intent)
                    }
                }
            }
        }
    }

    selectedDirectory?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                onSelectDirectory(it.uri)
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(context, intent, null)
            }
        }
    }
}


@SuppressLint("UnrememberedMutableState")
@Composable
@Preview
internal fun FileSizePreview() {
    KryptTheme {
        FileSizeView(modifier = Modifier, fileSizeState = mutableStateOf("500 MB"))
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
@Preview
private fun DeleteBackupFileDialogPreview() {
    KryptTheme {
        DeleteBackupFileDialog(
            modifier = Modifier,
            deleteDialogState = mutableStateOf(true to R.string.delete_backup_file),
            onDeleteBackUp = {},
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
@Preview
internal fun BackupViewPreview() {
    KryptTheme {
        BackupView(
            modifier = Modifier,
            lastBackupState = mutableStateOf("2022 April 22 - 22:30"),
            backupState = mutableStateOf(null),
            backupNowClick = {},
        )
    }
}

@Composable
@Preview
private fun DataScreenScaffoldPreview() {
    KryptTheme {
        DataScreenScaffold(
            modifier = Modifier,
            onNavigationClicked = {},
            snackbarHostState = remember { SnackbarHostState() },
        ) {

        }
    }
}

@Composable
@Preview
private fun DirectoryChooserPreview() {
    KryptTheme {
        ChooseDirectoryView(
            coroutineScope = rememberCoroutineScope(),
            snackbarHostState = remember { SnackbarHostState() },
            onSelectDirectory = {})
    }
}


