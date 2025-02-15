package dev.mslalith.focuslauncher.data.managers

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import dev.mslalith.focuslauncher.data.models.BottomSheetContentType
import dev.mslalith.focuslauncher.data.models.ConfirmDialogProperties
import dev.mslalith.focuslauncher.extensions.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
class LauncherViewManager constructor(
    val coroutineScope: CoroutineScope,
    val scaffoldState: ScaffoldState,
    val bottomSheetState: ModalBottomSheetState,
) {

    private val _sheetContentTypeFlow = MutableStateFlow<BottomSheetContentType?>(null)
    val sheetContentTypeStateFlow = _sheetContentTypeFlow.asStateFlow()

    private val _dialogPropertiesFlow = MutableStateFlow<ConfirmDialogProperties?>(null)
    val dialogPropertiesStateFlow = _dialogPropertiesFlow.asStateFlow()

    val isVisible: Boolean
        get() = bottomSheetState.isVisible

    val isDialogVisible: Boolean
        get() = _dialogPropertiesFlow.value != null

    fun hideBottomSheet() {
        coroutineScope
            .launch { bottomSheetState.hide() }
            .invokeOnCompletion { _sheetContentTypeFlow.value = null }
    }

    fun showBottomSheet(sheetType: BottomSheetContentType) {
        _sheetContentTypeFlow.value = sheetType
        coroutineScope.launch { bottomSheetState.animateTo(ModalBottomSheetValue.Expanded) }
    }

    fun hideDialog() {
        _dialogPropertiesFlow.value = null
    }

    fun showDialog(properties: ConfirmDialogProperties) {
        _dialogPropertiesFlow.value = properties
    }


    /**
     * SnackBars
     */
    suspend fun showSnackbar(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        dismissVisibleSnackbar: Boolean = false,
        discardIfShowing: Boolean = false,
        actionLabel: String? = null,
        onAction: ((SnackbarResult) -> Unit)? = null
    ) = scaffoldState.showSnackbar(
        message = message,
        duration = duration,
        dismissVisibleSnackbar = dismissVisibleSnackbar,
        discardIfShowing = discardIfShowing,
        actionLabel = actionLabel,
        onAction = onAction,
    )
}
