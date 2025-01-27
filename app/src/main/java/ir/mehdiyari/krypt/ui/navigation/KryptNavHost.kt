package ir.mehdiyari.krypt.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import ir.mehdiyari.krypt.backup.dataScreen
import ir.mehdiyari.krypt.features.auth.create_account.createAccountScreen
import ir.mehdiyari.krypt.features.auth.create_account.navigateToCreateAccount
import ir.mehdiyari.krypt.features.auth.login.loginScreen
import ir.mehdiyari.krypt.features.auth.login.navigateToLogin
import ir.mehdiyari.krypt.features.text.add.addTextScreen
import ir.mehdiyari.krypt.features.text.add.navigateToAddText
import ir.mehdiyari.krypt.features.text.list.navigateToTexts
import ir.mehdiyari.krypt.features.text.list.textsScreen
import ir.mehdiyari.krypt.home.ROUTE_HOME
import ir.mehdiyari.krypt.home.homeScreen
import ir.mehdiyari.krypt.home.navigateToHome
import ir.mehdiyari.krypt.mediaList.mediaScreen
import ir.mehdiyari.krypt.mediaList.navigateToMedia
import ir.mehdiyari.krypt.restore.navigateToRestore
import ir.mehdiyari.krypt.restore.restoreScreen
import ir.mehdiyari.krypt.setting.ui.settingsRoute
import ir.mehdiyari.krypt.shareContent.ShareDataViewModel
import ir.mehdiyari.krypt.shared.designsystem.components.snackbar.KryptSnackBar
import ir.mehdiyari.krypt.ui.KryptAppState
import ir.mehdiyari.krypt.voice.collection.audiosRoute
import ir.mehdiyari.krypt.voice.collection.navigateToAudios
import ir.mehdiyari.krypt.voice.record.record.addVoiceScreen
import ir.mehdiyari.krypt.voice.record.record.navigateToAddVoice

@Composable
fun KryptNaveHost(
    kryptAppState: KryptAppState,
    onShowSnackbar: (KryptSnackBar) -> Unit,
    startDestination: String,
    modifier: Modifier = Modifier,
    sharedDataViewModel: ShareDataViewModel,
    onStopLocker: () -> Unit,
    onRestartApp: () -> Unit,
) {
    val navController = kryptAppState.navController
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        loginScreen(onCreateAccountClicked = {
            navController.navigateToCreateAccount()
        }, onLoginSuccess = {
            navController.navigateToHome()
        }, showSnackBar = onShowSnackbar, onRestoreClicked = {
            navController.navigateToRestore()
        })

        createAccountScreen(onCreateAccountSuccess = {
            navController.navigateToLogin()
        }, onShowSnackbar = onShowSnackbar, onRestoreClicked = {
            navController.navigateToRestore()
        })

        homeScreen(
            sharedDataViewModel = sharedDataViewModel,
            openAddTextScreen = {
                navController.navigateToAddText(sharedText = it ?: "")
            },
            openTextsScreen = {
                navController.navigateToTexts()
            },
            openMediaScreen = { mediaViewAction ->
                navController.navigateToMedia(
                    mediaViewAction
                )
            },
            openMusicAndAudioScreen = {
                navController.navigateToAudios()
            },
        )

        textsScreen(onTextClick = {
            navController.navigateToAddText(textId = it)
        }, onNewNoteClick = {
            navController.navigateToAddText()
        }, onBackPressed = {
            navController.popBackStack()
        })

        addTextScreen { navController.popBackStack() }
        mediaScreen(
            sharedDataViewModel = sharedDataViewModel,
            { navController.popBackStack(ROUTE_HOME, false) },
            onStopLocker = onStopLocker
        )
        dataScreen { navController.popBackStack() }
        addVoiceScreen { navController.popBackStack() }
        audiosRoute({ navController.popBackStack() }, { navController.navigateToAddVoice() })
        settingsRoute(onRestartApp) { navController.popBackStack() }
        restoreScreen(onBackPressed = { navController.popBackStack() }, onRestart = onRestartApp)
    }
}

