package com.agileburo.anytype.core_ui.widgets.text

import android.R
import android.content.Context
import android.graphics.Canvas
import android.text.InputType
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.text.Spanned
import android.text.TextWatcher
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.graphics.withTranslation
import com.agileburo.anytype.core_ui.tools.ClipboardInterceptor
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.tools.MentionTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.highlight.HighlightAttributeReader
import com.agileburo.anytype.core_ui.widgets.text.highlight.HighlightDrawer
import com.agileburo.anytype.core_utils.ext.multilineIme
import com.agileburo.anytype.core_utils.ext.toast
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import timber.log.Timber

class TextInputWidget : AppCompatEditText {

    companion object {
        const val TEXT_INPUT_WIDGET_ACTION_GO = EditorInfo.IME_ACTION_GO
        const val TEXT_INPUT_WIDGET_INPUT_TYPE = TYPE_TEXT_FLAG_MULTI_LINE
    }

    private val watchers: MutableList<TextWatcher> = mutableListOf()

    private var highlightDrawer: HighlightDrawer? = null

    var selectionDetector: ((IntRange) -> Unit)? = null

    var clipboardInterceptor: ClipboardInterceptor? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup()
        setupHighlightHelpers(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        setup()
        setupHighlightHelpers(context, attrs)
    }

    private fun setup() {
        enableEditMode()
    }

    fun enableEditMode() {
        multilineIme(
            action = TEXT_INPUT_WIDGET_ACTION_GO,
            inputType = TEXT_INPUT_WIDGET_INPUT_TYPE
        )
        setTextIsSelectable(true)
    }

    fun enableReadMode() {
        inputType = InputType.TYPE_NULL
        setRawInputType(InputType.TYPE_NULL)
        maxLines = Integer.MAX_VALUE
        setHorizontallyScrolling(false)
        setTextIsSelectable(false)
    }

    private fun setupHighlightHelpers(context: Context, attrs: AttributeSet) {
        HighlightAttributeReader(context, attrs).let { reader ->
            highlightDrawer = HighlightDrawer(
                horizontalPadding = reader.horizontalPadding,
                verticalPadding = reader.verticalPadding,
                drawable = reader.drawable,
                drawableLeft = reader.drawableLeft,
                drawableMid = reader.drawableMid,
                drawableRight = reader.drawableRight
            )
        }
    }

    override fun addTextChangedListener(watcher: TextWatcher) {
        watchers.add(watcher)
        super.addTextChangedListener(watcher)
    }

    override fun removeTextChangedListener(watcher: TextWatcher) {
        watchers.remove(watcher)
        super.removeTextChangedListener(watcher)
    }

    fun clearTextWatchers() {
        watchers.forEach { super.removeTextChangedListener(it) }
        watchers.clear()
    }

    fun dismissMentionWatchers() {
        watchers.filterIsInstance(MentionTextWatcher::class.java).forEach { it.onDismiss() }
    }

    fun pauseTextWatchers(block: () -> Unit) = synchronized(this) {
        lockTextWatchers()
        block()
        unlockTextWatchers()
    }

    private fun lockTextWatchers() {
        watchers.forEach { watcher ->
            if (watcher is DefaultTextWatcher) watcher.lock()
        }
    }

    private fun unlockTextWatchers() {
        watchers.forEach { watcher ->
            if (watcher is DefaultTextWatcher) watcher.unlock()
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        Timber.d("New selection: $selStart - $selEnd")
        selectionDetector?.invoke(selStart..selEnd)
        super.onSelectionChanged(selStart, selEnd)
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (clipboardInterceptor == null) {
            return super.onTextContextMenuItem(id)
        }

        var consumed = false

        when(id) {
            R.id.paste -> {
                if (clipboardInterceptor != null) {
                    clipboardInterceptor?.onClipboardAction(
                        ClipboardInterceptor.Action.Paste(
                            selection = selectionStart..selectionEnd
                        )
                    )
                    consumed = true
                }
            }
            R.id.copy -> {
                if (clipboardInterceptor != null) {
                    clipboardInterceptor?.onClipboardAction(
                        ClipboardInterceptor.Action.Copy(
                            selection = selectionStart..selectionEnd
                        )
                    )
                    consumed = true
                }
            }
        }

        return if (!consumed) {
            super.onTextContextMenuItem(id)
        } else {
            consumed
        }
    }

    override fun onDraw(canvas: Canvas?) {
        // need to draw bg first so that text can be on top during super.onDraw()
        if (text is Spanned && layout != null) {
            canvas?.withTranslation(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat()) {
                highlightDrawer?.draw(canvas, text as Spanned, layout)
            }
        }
        super.onDraw(canvas)
    }

    fun setLinksClickable() {
        makeLinksActive()
    }

    /**
     *  Makes all links in the TextView object active.
     */
    private fun makeLinksActive() {
        BetterLinkMovementMethod.linkify(Linkify.ALL, this)
            .setOnLinkClickListener { textView, url ->
                textView.context.toast("On link click $url")
                false
            }
    }
}