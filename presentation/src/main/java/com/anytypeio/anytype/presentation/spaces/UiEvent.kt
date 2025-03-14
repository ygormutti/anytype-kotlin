package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.Id

sealed class UiEvent {
    data object OnBackPressed : UiEvent()

    data class OnSaveDescriptionClicked(val description: String) : UiEvent()
    data class OnSaveTitleClicked(val title: String) : UiEvent()
    data class OnSpaceImagePicked(val uri: String) : UiEvent()
    data object OnSelectWallpaperClicked : UiEvent()

    data object OnSpaceMembersClicked : UiEvent()
    data class OnDefaultObjectTypeClicked(val currentDefaultObjectTypeId: Id?) : UiEvent()

    data object OnDeleteSpaceClicked : UiEvent()
    data object OnRemoteStorageClick : UiEvent()
    data object OnPersonalizationClicked : UiEvent()
    data object OnInviteClicked : UiEvent()
    data object OnQrCodeClicked : UiEvent()

    sealed class IconMenu : UiEvent() {
        data object OnRemoveIconClicked : IconMenu()
    }

}