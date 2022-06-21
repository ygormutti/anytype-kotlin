package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockHeaderTwoBinding
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType

class HeaderTwo(
    val binding: ItemBlockHeaderTwoBinding,
    clicked: (ListenerType) -> Unit,
) : Header(binding.root, clicked) {

    override val header: TextInputWidget = binding.headerTwo
    override val content: TextInputWidget get() = header
    override val root: View = itemView

    private val mentionIconSize: Int
    private val mentionIconPadding: Int
    private val mentionCheckedIcon: Drawable?
    private val mentionUncheckedIcon: Drawable?
    private val mentionInitialsSize: Float

    init {
        setup()
        with(itemView.context) {
            mentionIconSize =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_two)
            mentionIconPadding =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_header_two)
            mentionUncheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_0_h2)
            mentionCheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_1_h2)
            mentionInitialsSize =
                resources.getDimension(R.dimen.mention_span_initials_size_header_two)
        }
    }

    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize
}