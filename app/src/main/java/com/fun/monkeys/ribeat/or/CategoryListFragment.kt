package com.`fun`.monkeys.ribeat.or

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.`fun`.monkeys.ribeat.or.data.Category
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.category_list.view.*
import kotlinx.android.synthetic.main.category_list_content.view.*

import android.graphics.Rect
import android.util.Base64
import android.widget.ImageView


/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [CategoryDetailMainFragment] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class CategoryListFragment : Fragment() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false
    private var compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.category_list, container, false)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        if (rootView.item_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }
        if (rootView.item_list.layoutManager is GridLayoutManager) {
            rootView.item_list.layoutManager = GridLayoutManager(rootView.context, 2)
            rootView.item_list.addItemDecoration(GridSpacingItemDecoration(2, 32, false))
        } else {

            rootView.item_list.addItemDecoration(GridSpacingItemDecoration(1, 48, true))
        }
        setupRecyclerView(rootView.item_list)
        return rootView
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        if (recyclerView.adapter == null) {
            recyclerView.adapter = SimpleItemRecyclerViewAdapter(
                this,
                Category.Manager.items.sortedBy { it.name },
                twoPane
            )
        } else {
            recyclerView.swapAdapter(SimpleItemRecyclerViewAdapter(
                this,
                Category.Manager.items.sortedBy { it.name },
                twoPane
            ), false)
        }

        if (Category.Manager.count == 0) {
            Category.Manager.fetchItems().subscribe { items ->
                recyclerView.swapAdapter(
                    SimpleItemRecyclerViewAdapter(
                        this,
                        items.sortedBy { it.name },
                        twoPane
                    ), false
                )
            }.addTo(compositeDisposable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    inner class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view) // item position
            val column = position % spanCount // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                outRect.right =
                    spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }
        }
    }

    inner class SimpleItemRecyclerViewAdapter(
        private val parentFragment: CategoryListFragment,
        var values: List<Category>,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as Category
                if (twoPane) {
                    val fragment = CategoryDetailFragment().apply {
                        arguments = Bundle().apply {
                            putInt(CategoryDetailFragment.ARG_ITEM_ID, item.id)
                        }
                    }
                    parentFragment.fragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.item_detail_container, fragment)
                        ?.commit()
                } else {
                    val fragment = CategoryDetailFragment().apply {
                        arguments = Bundle().apply {
                            putInt(CategoryDetailFragment.ARG_ITEM_ID, item.id)
                        }
                    }
                    parentFragment.fragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.item_list, fragment)
                        ?.addToBackStack(null)
                        ?.commit()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.category_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            val decoded = Base64.decode(item.image.toByteArray(), 0)
//            println(decoded)
//            println(item.image)
            holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.size))
            holder.contentView.text = item.name

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.category_image
            val contentView: TextView = view.content
        }
    }
}
