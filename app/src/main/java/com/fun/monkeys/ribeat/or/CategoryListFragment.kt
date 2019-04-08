package com.`fun`.monkeys.ribeat.or

import android.content.Context
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
import android.net.Uri
import android.util.Base64
import android.widget.ImageView
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.activity_main.*


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

    private var item: Category? = null
    private var twoPane: Boolean = false
    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(CategoryListFragment.ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = Category.Manager[it.getString(CategoryListFragment.ARG_ITEM_ID)?.toInt()!!]
                toolbar?.title = item?.name
            }
        }

    }

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

            rootView.item_list.addItemDecoration(GridSpacingItemDecoration(1, 24, true))
        }
        setupRecyclerView(rootView.item_list)
        if(item == null) item = (context as MainActivity).lastCategory
        if (item != null && twoPane) goToDetail(item!!, false)
        return rootView
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        if (recyclerView.adapter == null) {
            recyclerView.adapter = SimpleItemRecyclerViewAdapter(
                Category.Manager.items.sortedBy { it.name }
            )
        } else {
            recyclerView.swapAdapter(SimpleItemRecyclerViewAdapter(
                Category.Manager.items.sortedBy { it.name }
            ), false)
        }

        if (Category.Manager.count == 0) {
            Category.Manager.fetchItems().subscribe { items ->
                recyclerView.swapAdapter(
                    SimpleItemRecyclerViewAdapter(
                        items.sortedBy { it.name }
                    ), false
                )
            }.addTo(compositeDisposable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private lateinit var listener: OnFragmentInteractionListener


    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement OnItemSelectListener")
        }

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

    fun goToDetail(item: Category, animation: Boolean = true) {
        if (twoPane) {
            val fragment = CategoryDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(CategoryDetailFragment.ARG_ITEM_ID, item.id)
                }
            }
            fragmentManager
                ?.beginTransaction()
                ?.apply { if(animation) setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left) }
                ?.replace(R.id.item_detail_container, fragment)
                ?.addToBackStack(null)
                ?.commit()
        } else {

            NavHostFragment.findNavController(this).navigate(R.id.action_categoryListFragment_to_categoryDetailFragment, Bundle().apply {
                putInt(CategoryDetailFragment.ARG_ITEM_ID, item.id)
            })
        }
    }

    inner class SimpleItemRecyclerViewAdapter(
        var values: List<Category>
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as Category
                goToDetail(item)
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

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        val ARG_ITEM_ID = "item_id"
    }
}
