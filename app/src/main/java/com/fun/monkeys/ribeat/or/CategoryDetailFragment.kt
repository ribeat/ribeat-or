package com.`fun`.monkeys.ribeat.or

import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.`fun`.monkeys.ribeat.or.data.Category
import com.`fun`.monkeys.ribeat.or.data.Product
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.category_detail.view.*
import kotlinx.android.synthetic.main.category_list_content.view.*
import kotlinx.android.synthetic.main.product.view.*
import kotlin.math.max

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [CategoryListFragment]
 * in two-pane mode (on tablets) or a [CategoryDetailMainFragment]
 * on handsets.
 */
class CategoryDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: Category? = null
    private var products: List<Product>? = null
    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = Category.Manager[it.getInt(ARG_ITEM_ID)]
                toolbar?.title = item?.name
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.category_detail, container, false)

        // Show the dummy content as text in a TextView.
        item?.let {
            rootView.item_detail.text = item?.name
        }

        rootView.product_list.addItemDecoration(GridSpacingItemDecoration(1, 16, true))
        setupRecyclerView(rootView.product_list)

        return rootView
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        if (recyclerView.adapter == null) {
            recyclerView.adapter = SimpleItemRecyclerViewAdapter(
                Product.Manager.categoryMap[item?.id]?.sortedBy { it.name } ?: listOf()
            )
        } else {
            recyclerView.swapAdapter(
                SimpleItemRecyclerViewAdapter(
                    Product.Manager.categoryMap[item?.id]?.sortedBy { it.name } ?: listOf()
                ), false)
        }

        if (Product.Manager.count == 0) {
            Product.Manager.fetchItems().subscribe { _ ->
                recyclerView.swapAdapter(
                    SimpleItemRecyclerViewAdapter(
                        Product.Manager.categoryMap[item?.id]?.sortedBy { it.name } ?: listOf()
                    ), false
                )
            }.addTo(compositeDisposable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    inner class SimpleItemRecyclerViewAdapter(
        var values: List<Product>
    ) : RecyclerView.Adapter<CategoryDetailFragment.SimpleItemRecyclerViewAdapter.ViewHolder>() {

//        private val onClickListener: View.OnClickListener

        init {
//            onClickListener = View.OnClickListener { v ->
//                val item = v.tag as Category
//                val fragment = CategoryDetailFragment().apply {
//                        arguments = Bundle().apply {
//                            putInt(CategoryDetailFragment.ARG_ITEM_ID, item.id)
//                        }
//                    }
//                    parentFragment.fragmentManager
//                        ?.beginTransaction()
//                        ?.replace(R.id.item_list, fragment)
//                        ?.addToBackStack(null)
//                        ?.commit()
//                }
//            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.product, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            val decoded = Base64.decode(item.image.toByteArray(), 0)
            holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.size))
            holder.priceView.text = getString(R.string.lei, item.price)
            holder.nameView.text = item.name
            holder.detailsView.text = item.details
            holder.orderCount.setText(0.toString())

            with(holder.itemView) {
                tag = item
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView = view.product_image
            val priceView = view.product_price
            val nameView = view.product_name
            val detailsView = view.product_details
            val minusButton = view.order_minus_button
            val plusButton = view.order_plus_button
            val orderCount = view.order_count
            val orderButtton = view.order_button

            init {
                minusButton.setOnClickListener {
                    orderCount.setText(max(0, orderCount.text.toString().toInt() - 1).toString())
                }
                plusButton.setOnClickListener {
                    orderCount.setText((orderCount.text.toString().toInt() + 1).toString())
                }
                orderButtton.setOnClickListener {
                    val count = orderCount.text.toString().toInt()
                    orderCount.setText(0.toString())
                }
            }
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

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}
