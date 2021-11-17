package com.anytypeio.anytype.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ui.DashboardSpacingItemDecoration
import kotlinx.android.synthetic.main.item_dashboard_page.view.*
import kotlinx.android.synthetic.main.item_dashboard_page_archived.view.*
import kotlinx.android.synthetic.main.item_dashboard_recent.view.*
import kotlinx.android.synthetic.main.item_dashboard_sets.view.*
import kotlinx.android.synthetic.main.item_dashboard_shared.view.*

class DashboardPager(
    private var items: List<TabItem>,
    private val defaultAdapter: DashboardAdapter,
    private val recentAdapter: DashboardAdapter,
    private val setsAdapter: DashboardAdapter,
    private val archiveAdapter: DashboardAdapter,
    private val sharedAdapter: DashboardAdapter,
    private val dndBehavior: DashboardDragAndDropBehavior
): RecyclerView.Adapter<DashboardPager.ViewHolder>() {

    fun getTitle(position: Int) : String = items[position].title

    fun setItems(items: List<TabItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = when(viewType) {
        R.layout.item_dashboard_page -> {
            ViewHolder.Default(parent).apply {
                itemView.rvDashboard.apply {
                    val spacing = itemView.context.dimen(R.dimen.default_dashboard_item_spacing).toInt()
                    layoutManager = GridLayoutManager(context, COLUMN_COUNT)
                    overScrollMode = OVER_SCROLL_NEVER
                    addItemDecoration(DashboardSpacingItemDecoration(spacing))
                    setHasFixedSize(true)
                    adapter = defaultAdapter
                    ItemTouchHelper(dndBehavior).attachToRecyclerView(this)
                }
            }
        }
        R.layout.item_dashboard_recent -> {
            ViewHolder.Recent(parent).apply {
                itemView.rvDashboardRecent.apply {
                    val spacing = itemView.context.dimen(R.dimen.default_dashboard_item_spacing).toInt()
                    layoutManager = GridLayoutManager(context, COLUMN_COUNT)
                    overScrollMode = OVER_SCROLL_NEVER
                    addItemDecoration(DashboardSpacingItemDecoration(spacing))
                    setHasFixedSize(true)
                    adapter = recentAdapter
                }
            }
        }
        R.layout.item_dashboard_sets -> {
            ViewHolder.Sets(parent).apply {
                itemView.rvDashboardSets.apply {
                    val spacing = itemView.context.dimen(R.dimen.default_dashboard_item_spacing).toInt()
                    layoutManager = GridLayoutManager(context, COLUMN_COUNT)
                    overScrollMode = OVER_SCROLL_NEVER
                    addItemDecoration(DashboardSpacingItemDecoration(spacing))
                    setHasFixedSize(true)
                    adapter = setsAdapter
                }
            }
        }
        R.layout.item_dashboard_page_archived -> {
            ViewHolder.Archived(parent).apply {
                itemView.rvDashboardArchived.apply {
                    val spacing = itemView.context.dimen(R.dimen.default_dashboard_item_spacing).toInt()
                    layoutManager = GridLayoutManager(context, COLUMN_COUNT)
                    overScrollMode = OVER_SCROLL_NEVER
                    addItemDecoration(DashboardSpacingItemDecoration(spacing))
                    setHasFixedSize(true)
                    adapter = archiveAdapter
                }
            }
        }
        R.layout.item_dashboard_shared -> {
            ViewHolder.Shared(parent).apply {
                itemView.rvDashboardShared.apply {
                    val spacing = itemView.context.dimen(R.dimen.default_dashboard_item_spacing).toInt()
                    layoutManager = GridLayoutManager(context, COLUMN_COUNT)
                    overScrollMode = OVER_SCROLL_NEVER
                    addItemDecoration(DashboardSpacingItemDecoration(spacing))
                    setHasFixedSize(true)
                    adapter = sharedAdapter
                }
            }
        }
         else -> throw IllegalStateException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int) = when(items[position].type) {
        TYPE_FAVOURITES -> R.layout.item_dashboard_page
        TYPE_RECENT -> R.layout.item_dashboard_recent
        TYPE_SETS-> R.layout.item_dashboard_sets
        TYPE_BIN -> R.layout.item_dashboard_page_archived
        TYPE_SHARED -> R.layout.item_dashboard_shared
        else -> throw IllegalStateException("Unexpected item: ${items[position]}")
    }

    companion object {
        const val COLUMN_COUNT = 2
        const val TYPE_FAVOURITES = 0
        const val TYPE_RECENT = 1
        const val TYPE_SETS = 3
        const val TYPE_BIN = 4
        const val TYPE_SHARED = 5
    }

    sealed class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        class Default(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dashboard_page,
                parent,
                false
            )
        )
        class Recent(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dashboard_recent,
                parent,
                false
            )
        )
        class Sets(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dashboard_sets,
                parent,
                false
            )
        )
        class Archived(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dashboard_page_archived,
                parent,
                false
            )
        )
        class Shared(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dashboard_shared,
                parent,
                false
            )
        )
    }
}