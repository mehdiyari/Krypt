package ir.mehdiyari.krypt.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.mehdiyari.krypt.data.repositories.AccountsRepository
import ir.mehdiyari.krypt.data.repositories.SettingsRepository
import ir.mehdiyari.krypt.di.qualifiers.DispatcherIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountsRepository,
    private val deleteAccountHelper: DeleteAccountHelper,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _automaticallyLockSelectedItem =
        MutableStateFlow(settingsRepository.getLockAutomaticallyValue())

    val automaticallyLockSelectedItem = _automaticallyLockSelectedItem.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<DeleteAccountViewState?>(null)
    val deleteAccountState = _deleteAccountState.asStateFlow()

    fun onSelectAutoLockItem(autoLockItemsEnum: AutoLockItemsEnum) {
        viewModelScope.launch {
            _automaticallyLockSelectedItem.emit(autoLockItemsEnum)
            settingsRepository.storeLockAutomatically(autoLockItemsEnum)
        }
    }

    fun onDeleteCurrentAccount(password: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                if (accountRepository.validatePassword(password)) {
                    _deleteAccountState.emit(DeleteAccountViewState.DeleteAccountStarts)
                    deleteAccountHelper.clearCurrentAccount()
                    _deleteAccountState.emit(DeleteAccountViewState.DeleteAccountFinished)
                } else {
                    _deleteAccountState.emit(DeleteAccountViewState.PasswordsNotMatch)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _deleteAccountState.emit(DeleteAccountViewState.DeleteAccountFailed)
            }
        }
    }
}