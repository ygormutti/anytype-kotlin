package com.anytypeio.anytype.ui.spaces

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_models.PRIVATE_SPACE_TYPE
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.ui_settings.space.TypeOfSpace

@Composable
fun CreateSpaceScreen(
    spaceIconView: SpaceIconView.Placeholder,
    onCreate: (Name, IsSpaceLevelChatSwitchChecked) -> Unit,
    onSpaceIconClicked: () -> Unit,
    isLoading: State<Boolean>
) {

    var isSpaceLevelChatSwitchChecked = remember { mutableStateOf(false) }

    val input = remember {
        mutableStateOf("")
    }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.TopCenter)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp)
        ) {
            Header()
            Spacer(modifier = Modifier.height(16.dp))
            SpaceIcon(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                spaceIconView = spaceIconView.copy(
                    name = input.value.ifEmpty { 
                        stringResource(id = R.string.s)
                    }
                ),
                onSpaceIconClicked = onSpaceIconClicked
            )
            Spacer(modifier = Modifier.height(10.dp))
            SpaceNameInput(
                input = input,
                onActionDone = {
                    focusManager.clearFocus()
                    onCreate(input.value, isSpaceLevelChatSwitchChecked.value)
                }
            )
            Divider()
            Section(title = stringResource(id = R.string.type))
            TypeOfSpace(spaceType = PRIVATE_SPACE_TYPE)
            Divider()
            if (BuildConfig.DEBUG) {
                DebugCreateSpaceLevelChatToggle(isSpaceLevelChatSwitchChecked)
            }
            Spacer(modifier = Modifier.height(78.dp))
        }
        CreateSpaceButton(
            onCreate = { name ->
                focusManager.clearFocus()
                onCreate(name, isSpaceLevelChatSwitchChecked.value)
            },
            input = input,
            modifier = Modifier.align(Alignment.BottomCenter),
            isLoading = isLoading
        )
    }
}

@Composable
private fun DebugCreateSpaceLevelChatToggle(isChatToggleChecked: MutableState<Boolean>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp)
    ) {
        Switch(
            checked = isChatToggleChecked.value,
            onCheckedChange = {
                isChatToggleChecked.value = it
            },
            colors = SwitchDefaults.colors().copy(
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent,
                checkedTrackColor = colorResource(R.color.palette_system_amber_50),
                uncheckedTrackColor = colorResource(R.color.shape_secondary)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Enable space-level chat (dev mode)",
            color = colorResource(id = com.anytypeio.anytype.ui_settings.R.color.text_primary),
            style = BodyRegular
        )
    }
}

@Composable
private fun CreateSpaceButton(
    modifier: Modifier,
    onCreate: (Name) -> Unit,
    input: State<String>,
    isLoading: State<Boolean>
) {
    Box(
        modifier = modifier
            .height(78.dp)
            .fillMaxWidth()
    ) {
        ButtonPrimaryLoading(
            onClick = { onCreate(input.value) },
            text = stringResource(id = R.string.create),
            size = ButtonSize.Large,
            modifierButton = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
            ,
            modifierBox = Modifier
                .padding(bottom = 10.dp)
                .align(Alignment.BottomCenter)
            ,
            loading = isLoading.value
        )
    }
}

@Composable
fun Header() {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.create_space),
            style = Title2,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
fun SpaceIcon(
    modifier: Modifier,
    spaceIconView: SpaceIconView,
    onSpaceIconClicked: () -> Unit
) {
    Box(modifier = modifier.wrapContentSize()) {
        SpaceIconView(
            icon = spaceIconView,
            onSpaceIconClick = onSpaceIconClicked
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SpaceNameInput(
    input: MutableState<String>,
    onActionDone: () -> Unit
) {
    val focusRequester = FocusRequester()
    Box(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
    ) {
        BasicTextField(
            value = input.value,
            onValueChange = { input.value = it },
            keyboardActions = KeyboardActions(
                onDone = { onActionDone() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, bottom = 12.dp)
                .align(Alignment.BottomStart)
                .focusRequester(focusRequester)
            ,
            maxLines = 1,
            singleLine = true,
            textStyle = HeadlineHeading.copy(
                color = colorResource(id = R.color.text_primary)
            ),
            cursorBrush = SolidColor(
                colorResource(id = R.color.cursor_color)
            ),
            decorationBox = @Composable { innerTextField ->
                TextFieldDefaults.OutlinedTextFieldDecorationBox(
                    value = input.value,
                    innerTextField = innerTextField,
                    singleLine = true,
                    enabled = true,
                    isError = false,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.space_name),
                            style = HeadlineHeading
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = colorResource(id = R.color.text_primary),
                        backgroundColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        placeholderColor = colorResource(id = R.color.text_tertiary)
                    ),
                    contentPadding = PaddingValues(
                        start = 0.dp,
                        top = 0.dp,
                        end = 0.dp,
                        bottom = 0.dp
                    ),
                    border = {},
                    interactionSource = remember { MutableInteractionSource() },
                    visualTransformation = VisualTransformation.None
                )
            }
        )
        Text(
            color = colorResource(id = R.color.text_secondary),
            style = Caption1Regular,
            modifier = Modifier.padding(
                start = 20.dp,
                top = 11.dp
            ),
            text = stringResource(id = R.string.space_name)
        )
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun Section(
    title: String,
    color: Color = colorResource(id = R.color.text_secondary),
    textPaddingStart: Dp = 20.dp
) {
    Box(modifier = Modifier
        .height(52.dp)
        .fillMaxWidth()) {
        Text(
            modifier = Modifier
                .padding(
                    start = textPaddingStart,
                    bottom = 8.dp
                )
                .align(Alignment.BottomStart),
            text = title,
            color = color,
            style = Caption1Regular
        )
    }
}

@Composable
fun UseCase() {
    Box(modifier = Modifier.height(52.dp)) {
        Text(
            modifier = Modifier
                .padding(start = 20.dp)
                .align(Alignment.CenterStart),
            text = "Empty space",
            color = colorResource(id = R.color.text_primary),
            style = BodyRegular
        )
    }
}

typealias IsSpaceLevelChatSwitchChecked = Boolean