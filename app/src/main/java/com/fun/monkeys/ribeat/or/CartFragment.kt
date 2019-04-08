package com.`fun`.monkeys.ribeat.or

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.`fun`.monkeys.ribeat.or.data.Order
import com.`fun`.monkeys.ribeat.or.data.Product
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.cart.view.*
import kotlinx.android.synthetic.main.cart_item.view.*
import kotlinx.android.synthetic.main.category_detail.view.*
import kotlin.math.max

class CartFragment : Fragment() {
    private var compositeDisposable = CompositeDisposable()

    var orders = OrderController.INSTANCE.getOrders()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.cart, container, false)

        rootView.order_list.addItemDecoration(GridSpacingItemDecoration(1, 16, false))
        setupRecyclerView(rootView.order_list)
        return rootView

    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        if (recyclerView.adapter == null) {
            recyclerView.adapter = SimpleItemRecyclerViewAdapter(
                orders
            )
        } else {
            recyclerView.swapAdapter(
                SimpleItemRecyclerViewAdapter(
                    orders
                ), false
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }


    inner class SimpleItemRecyclerViewAdapter(
        var values: List<Order>
    ) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cart_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            val product = Product.Manager[item.productId]
            val decoded = Base64.decode(product?.image?.toByteArray(), 0)
            holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(decoded, 0, decoded.size))
            holder.priceView.text = getString(R.string.lei, product?.price)
            holder.nameView.text = product?.name
            holder.detailsView.text = product?.details
            holder.orderTotal.text = getString(R.string.lei, product?.price!! * item.quantity)
            holder.orderCount.setText(item.quantity.toString())

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
            val orderTotal = view.order_total
            val deleteButton = view.order_delete_button

            init {
                minusButton.setOnClickListener {
                    orderCount.setText(max(0, orderCount.text.toString().toInt() - 1).toString())
                }
                plusButton.setOnClickListener {
                    orderCount.setText((orderCount.text.toString().toInt() + 1).toString())
                }
                orderButtton.setOnClickListener {
                    val count = orderCount.text.toString().toInt()
                    val order = itemView.tag as Order
                    order.quantity = count

                    (if (count == 0) OrderController.INSTANCE::removeOrder else OrderController.INSTANCE::updateOrder)(order)
                    if (count == 0) orders.remove(order)
                }
                deleteButton.setOnClickListener {
                    OrderController.INSTANCE.removeOrder(itemView.tag as Order)
                    orders.remove(itemView.tag as Order)
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

    private lateinit var listener: OnFragmentInteractionListener


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as OnFragmentInteractionListener
//            listener.onFragmentInteraction()
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement OnItemSelectListener")
        }

    }


    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
