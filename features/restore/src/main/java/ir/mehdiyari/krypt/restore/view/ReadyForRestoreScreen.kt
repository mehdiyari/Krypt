package ir.mehdiyari.krypt.restore.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mehdiyari.krypt.core.designsystem.theme.KryptTheme
import ir.mehdiyari.krypt.restore.R
import ir.mehdiyari.krypt.shared.designsystem.components.PasswordTextField
import ir.mehdiyari.krypt.shared.designsystem.resources.R as DesignSystemR

@Composable
internal fun ReadyForRestore(
    modifier: Modifier,
    filePath: String,
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onRestoreClicked: (password: String) -> Unit,
    onBackPressed: () -> Unit,
    loadingState: Boolean,
) {
    var password by remember { mutableStateOf("") }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        Column {
            RestoreTopBar(modifier, onBackPressed)
            FileAndPermissionPreview(
                modifier = modifier,
                filePath = filePath,
                permissionGranted = isPermissionGranted,
                onRequestPermission = onRequestPermission,
                loadingState = loadingState,
            )
            if (isPermissionGranted) {
                UserCredentials(
                    modifier = modifier,
                    password = password,
                    onPasswordChanged = {
                        password = it
                    },
                    isEnabled = !loadingState,
                )
            }
        }

        val context = LocalContext.current
        ExtendedFloatingActionButton(
            containerColor = MaterialTheme.colorScheme.primary,
            onClick = {
                if (isPermissionGranted) {
                    if (!loadingState) {
                        onRestoreClicked(password)
                    } else {
                        Toast.makeText(
                            context,
                            R.string.restore_loading_message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    onRequestPermission()
                }
            },
            icon = {
                if (!loadingState) {
                    Icon(
                        Icons.Filled.Done,
                        contentDescription = stringResource(id = R.string.restore_button)
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = modifier.size(30.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            text = {
                val text = if (!loadingState) {
                    R.string.restore_button
                } else {
                    R.string.restore_wait_button
                }
                Text(
                    text = stringResource(id = text),
                    fontSize = 12.sp,
                )
            },
            modifier = modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }
}

@Composable
fun UserCredentials(
    modifier: Modifier,
    password: String,
    onPasswordChanged: (String) -> Unit,
    isEnabled: Boolean
) {
    Column(
        modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp)
    ) {
        PasswordTextField(
            isEnabled = isEnabled,
            password = password,
            hint = stringResource(id = ir.mehdiyari.krypt.shared.designsystem.resources.R.string.account_password),
            onPasswordChanged = onPasswordChanged,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 5.dp)
        )
    }

}

@Composable
fun FileAndPermissionPreview(
    modifier: Modifier,
    filePath: String,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    loadingState: Boolean
) {
    Column(
        modifier = modifier.padding(12.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
            text = stringResource(id = R.string.restore_file_selected),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )

        Row {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = modifier.size(24.dp)
            )

            Text(text = filePath, Modifier.padding(start = 4.dp, end = 4.dp))
        }

        if (!permissionGranted) {
            val clickableModifier = if (!loadingState) {
                modifier.clickable {
                    onRequestPermission()
                }
            } else {
                modifier
            }

            Text(
                modifier = clickableModifier
                    .padding(top = 24.dp, bottom = 2.dp),
                text = stringResource(id = R.string.permission_required),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )

            Row(
                modifier = clickableModifier,
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = modifier.size(24.dp)
                )

                Text(
                    text = stringResource(id = DesignSystemR.string.manager_external_permission_description),
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RestoreTopBar(modifier: Modifier, onBackPressed: () -> Unit) {
    TopAppBar(title = {
        Text(text = stringResource(id = R.string.restore_title))
    }, navigationIcon = {
        Icon(
            Icons.Filled.ArrowBack,
            contentDescription = "",
            modifier = modifier
                .padding(4.dp)
                .clickable {
                    onBackPressed()
                }
        )
    })
}


@Preview
@Composable
private fun RestoreTopBarPreview() {
    KryptTheme {
        RestoreTopBar(Modifier) {

        }
    }
}

@Preview
@Composable
private fun ReadyForRestoreReview() {
    KryptTheme {
        ReadyForRestore(
            modifier = Modifier,
            filePath = "/downloads/ir/mehdiyari/files/personal/documents/backups/krypt/restore.krp",
            isPermissionGranted = false,
            {},
            { _ -> },
            {},
            loadingState = false,
        )
    }
}

@Preview
@Composable
private fun ReadyForRestoreReviewWithProgress() {
    KryptTheme {
        ReadyForRestore(
            modifier = Modifier,
            filePath = "/downloads/ir/mehdiyari/files/personal/documents/backups/krypt/restore.krp",
            isPermissionGranted = true,
            {},
            { _ -> },
            {},
            loadingState = true,
        )
    }
}


@Preview
@Composable
private fun FileAndPermissionPreviewPreview() {
    KryptTheme {
        FileAndPermissionPreview(
            Modifier,
            "/downloads/ir/mehdiyari/files/personal/documents/backups/krypt/restore.krp",
            false,
            {},
            false,
        )
    }
}