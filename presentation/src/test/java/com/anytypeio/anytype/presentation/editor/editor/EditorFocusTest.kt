package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubLayoutColumns
import com.anytypeio.anytype.core_models.StubLayoutRows
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubTable
import com.anytypeio.anytype.core_models.StubTableCells
import com.anytypeio.anytype.core_models.StubTableColumns
import com.anytypeio.anytype.core_models.StubTableRows
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.model.Focusable
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verifyNoInteractions


class EditorFocusTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = "Relation Block UI Testing",
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    @Test
    fun `should clear focus internally and re-render on hide-keyboard event`() = runTest {

        // SETUP

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values()
                    .filter { it != Block.Content.Text.Style.DESCRIPTION }.random()
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, block.id)
            ),
            header,
            title,
            block
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(page)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val testViewStateObserver = vm.state.test()

        testViewStateObserver.assertValue { value ->
            check(value is ViewState.Success)
            val last = value.blocks.last()
            check(last is Focusable)
            !last.isFocused
        }

        vm.onBlockFocusChanged(
            id = block.id,
            hasFocus = true
        )

        advanceUntilIdle()

        assertEquals(
            block.id,
            orchestrator.stores.focus.current().requireTarget()
        )

        vm.onHideKeyboardClicked()

        advanceUntilIdle()

        assertTrue(
            orchestrator.stores.focus.current().isEmpty
        )
    }

    @Test
    fun `should focus on start if title is empty`() = runTest {

        // SETUP

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id)
            ),
            header,
            title.copy(
                content = title.content<Block.Content.Text>().copy(
                    text = ""
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(page)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val testViewStateObserver = vm.state.test()

        testViewStateObserver.assertValue { value ->
            check(value is ViewState.Success)
            val last = value.blocks.last()
            check(last is Focusable)
            last.isFocused
        }

        assertEquals(
            title.id,
            orchestrator.stores.focus.current().requireTarget()
        )
    }

    @Test
    fun `should not focus on start if title is not empty`() = runTest {

        // SETUP

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id)
            ),
            header,
            title
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(page)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val testViewStateObserver = vm.state.test()

        testViewStateObserver.assertValue { value ->
            check(value is ViewState.Success)
            val last = value.blocks.last()
            check(last is Focusable)
            !last.isFocused
        }

        assertTrue(orchestrator.stores.focus.current().isEmpty)
    }

    @Test
    fun `should update views on hide-keyboard event`() = runTest {

        // SETUP

        val excludedStyles = listOf(
            Block.Content.Text.Style.DESCRIPTION,
            Block.Content.Text.Style.TITLE,
            Block.Content.Text.Style.CODE_SNIPPET
        )

        val style = Block.Content.Text.Style.values()
            .filter { style -> !excludedStyles.contains(style) }
            .random()

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(header.id, block.id)
            ),
            header,
            title,
            block
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(page)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.state.test().apply {
            assertValue { value ->
                check(value is ViewState.Success)
                val last = value.blocks.last()
                check(last is Focusable)
                !last.isFocused
            }
        }

        vm.onBlockFocusChanged(
            id = block.id,
            hasFocus = true
        )

        advanceUntilIdle()

        vm.onHideKeyboardClicked()

        advanceUntilIdle()

        vm.state.test().apply {
            assertValue { value ->
                check(value is ViewState.Success)
                val last = value.blocks.last()
                check(last is Focusable)
                !last.isFocused
            }
        }

        vm.onOutsideClicked()

        advanceUntilIdle()

        vm.state.test().apply {
            try {
                assertValue { value ->
                    check(value is ViewState.Success)
                    val last = value.blocks.last()
                    check(last is Focusable)
                    last.isFocused
                }
            } catch (e: AssertionError) {
                throw AssertionError("Test assertion failed for style: $style")
            }
        }

        verifyNoInteractions(createBlock)
    }

    @Test
    fun `should close keyboard and clear focus when system close keyboard happened`() = runTest {

        // SETUP

        val paragraph = StubParagraph()
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraph.id)
        )
        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = IntRange(0, 0)
        )

        advanceUntilIdle()

        vm.onBlockFocusChanged(
            id = paragraph.id,
            hasFocus = true
        )

        advanceUntilIdle()

        vm.onBackPressedCallback()

        advanceUntilIdle()

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState(
                navigationToolbar = ControlPanelState.Toolbar.Navigation(isVisible = true)
            )
        )
    }

    @Test
    fun `when set focus in text block - Main toolbar should be with Any type`() = runTest {

        // SETUP
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val first = StubParagraph()
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, first.id)
        )
        val doc = listOf(page, header, title, first)
        val vm = buildViewModel()

        // STUB
        stubInterceptEvents()
        stubOpenDocument(doc)
        stubUpdateText()

        // TESTING Click on text block
        vm.apply {
            onStart(id = root, space = defaultSpace)
            advanceUntilIdle()
            onBlockFocusChanged(
                id = first.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSelectionChanged(
                id = first.id,
                selection = IntRange(0, 0)
            )
            advanceUntilIdle()
        }

        // EXPECTED
        val expectedState = ControlPanelState.init().copy(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true,
                targetBlockType = ControlPanelState.Toolbar.Main.TargetBlockType.Any
            ),
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            )
        )

        // ASSERT
        assertEquals(
            expected = expectedState,
            actual = vm.controlPanelViewState.test().value()
        )
    }

    @Test
    fun `when set focus in title block - Main toolbar should be with Title type`() = runTest {

        // SETUP
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val first = StubParagraph()
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, first.id)
        )
        val doc = listOf(page, header, title, first)
        val vm = buildViewModel()

        // STUB
        stubInterceptEvents()
        stubOpenDocument(doc)
        stubUpdateText()

        // TESTING Click on title block
        vm.apply {
            onStart(id = root, space = defaultSpace)
            advanceUntilIdle()
            onBlockFocusChanged(
                id = title.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSelectionChanged(
                id = title.id,
                selection = IntRange(0, 0)
            )
            advanceUntilIdle()
        }

        // EXPECTED
        val expectedState = ControlPanelState.init().copy(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true,
                targetBlockType = ControlPanelState.Toolbar.Main.TargetBlockType.Title
            ),
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            )
        )

        // ASSERT
        assertEquals(
            expected = expectedState,
            actual = vm.controlPanelViewState.test().value()
        )
    }

    @Test
    fun `when set focus in cell block - Main toolbar should be with Cell type`() = runTest {

        // SETUP
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 2)
        val cells = StubTableCells(columns = columns, rows = rows)
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(children = listOf(columnLayout.id, rowLayout.id))
        val paragraph = StubParagraph()
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, table.id, paragraph.id) + listOf(table.id) + listOf(
                paragraph.id
            )
        )
        val doc = listOf(
            page,
            header,
            title,
            table,
            columnLayout,
            rowLayout
        ) + columns + rows + cells + paragraph
        val vm = buildViewModel()

        // STUB
        stubInterceptEvents()
        stubOpenDocument(doc)
        stubUpdateText()

        // TESTING Click on cell block
        vm.apply {
            onStart(id = root, space = defaultSpace)
            advanceUntilIdle()
            onBlockFocusChanged(
                id = cells[0].id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSelectionChanged(
                id = cells[0].id,
                selection = IntRange(0, 0)
            )
            advanceUntilIdle()
        }

        // EXPECTED
        val expectedState = ControlPanelState.init().copy(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true,
                targetBlockType = ControlPanelState.Toolbar.Main.TargetBlockType.Cell
            ),
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            )
        )

        // ASSERT
        assertEquals(
            expected = expectedState,
            actual = vm.controlPanelViewState.test().value()
        )
    }
}