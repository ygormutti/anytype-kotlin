package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockView

class DividerBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Divider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {}
    override fun getBlock(): BlockView = block
    override fun blockLayout(): Int = R.layout.item_block_divider_preview
}