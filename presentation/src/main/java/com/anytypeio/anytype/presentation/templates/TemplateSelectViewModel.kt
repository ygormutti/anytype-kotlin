package com.anytypeio.anytype.presentation.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsDefaultTemplateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSelectTemplateEvent
import com.anytypeio.anytype.presentation.objects.menu.ObjectMenuViewModelBase
import com.anytypeio.anytype.presentation.templates.TemplateView.Companion.DEFAULT_TEMPLATE_ID_BLANK
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class TemplateSelectViewModel(
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val getTemplates: GetTemplates,
    private val applyTemplate: ApplyTemplate,
    private val analytics: Analytics,
    private val setObjectDetails: SetObjectDetails
) : BaseViewModel() {

    val isDismissed = MutableStateFlow(false)

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Init)
    val viewState: StateFlow<ViewState> = _viewState

    fun onStart(typeId: Id) {
        viewModelScope.launch {
            val objType = storeOfObjectTypes.get(typeId)
            if (objType != null) {
                Timber.d("onStart, Object type $objType")
                proceedWithGettingTemplates(objType)
            } else {
                Timber.e("onStart, Object type $typeId not found")
            }
        }
    }

    private fun proceedWithGettingTemplates(
        objType: ObjectWrapper.Type
    ) {
        val params = GetTemplates.Params(
            type = TypeId(objType.id)
        )
        viewModelScope.launch {
            getTemplates.async(params).fold(
                onSuccess = { buildTemplateViews(objType, it) },
                onFailure = { Timber.e(it, "Error while getting templates") })
        }
    }

    private suspend fun buildTemplateViews(
        objType: ObjectWrapper.Type,
        templates: List<ObjectWrapper.Basic>
    ) {
        val templateViews = buildList {
            //turned off for now DROID-3340
//            add(
//                TemplateSelectView.Blank(
//                    typeId = objType.id,
//                    typeName = objType.name.orEmpty(),
//                    layout = objType.recommendedLayout?.code ?: 0
//                )
//            )
            addAll(templates.map {
                TemplateSelectView.Template(
                    id = it.id,
                    layout = it.layout ?: ObjectType.Layout.BASIC,
                    typeId = objType.id,
                    typeKey = objType.uniqueKey,
                    space = requireNotNull(it.spaceId)
                )
            })
        }
        _viewState.emit(
            ViewState.Success(
                objectTypeName = objType.name.orEmpty(),
                templates = templateViews,
            )
        )
    }

    fun onUseTemplateButtonPressed(ctx: Id, currentItem: Int) {
        when (val state = _viewState.value) {
            is ViewState.Success -> {
                when (val template = state.templates[currentItem]) {
                    is TemplateSelectView.Blank -> {
                        proceedWithApplyingTemplate(ctx, "")
                    }
                    is TemplateSelectView.Template -> {
                        proceedWithApplyingTemplate(ctx, template.id)
                    }
                }
                viewModelScope.launch {
                    sendAnalyticsSelectTemplateEvent(analytics)
                }
            }
            else -> {
                Timber.e("onUseTemplate: unexpected state $state")
                isDismissed.value = true
            }
        }
    }

    private fun proceedWithApplyingTemplate(ctx: Id, id: Id) {
        val params = ApplyTemplate.Params(ctx = ctx, template = id)
        viewModelScope.launch {
            applyTemplate.async(params).fold(
                onSuccess = {
                    isDismissed.value = true
                    Timber.d("Template ${id} applied successfully")
                },
                onFailure = {
                    isDismissed.value = true
                    Timber.e(it, "Error while applying template")
                    sendToast("Something went wrong. Please, try again later.")
                }
            )
        }
    }

    fun proceedWithSettingAsDefaultTemplate(typeId: Id) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            val objType = storeOfObjectTypes.get(typeId)
            viewModelScope.launch {
                val params = SetObjectDetails.Params(
                    ctx = typeId,
                    details = mapOf(Relations.DEFAULT_TEMPLATE_ID to DEFAULT_TEMPLATE_ID_BLANK)
                )
                setObjectDetails.async(params).fold(
                    onSuccess = {
                        sendAnalyticsDefaultTemplateEvent(analytics, objType, startTime)
                        _toasts.emit("The blank template was set as default")
                    },
                    onFailure = {
                        Timber.e(it, "Error while setting blank template as default")
                        _toasts.emit(ObjectMenuViewModelBase.SOMETHING_WENT_WRONG_MSG)
                    }
                )
            }
        }
    }

    class Factory @Inject constructor(
        private val applyTemplate: ApplyTemplate,
        private val getTemplates: GetTemplates,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val analytics: Analytics,
        private val setObjectDetails: SetObjectDetails
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TemplateSelectViewModel(
                applyTemplate = applyTemplate,
                getTemplates = getTemplates,
                storeOfObjectTypes = storeOfObjectTypes,
                analytics = analytics,
                setObjectDetails = setObjectDetails
            ) as T
        }
    }

    sealed class ViewState {
        data class Success(
            val objectTypeName: String, val templates: List<TemplateSelectView>
        ) : ViewState()

        object Init : ViewState()
    }

}