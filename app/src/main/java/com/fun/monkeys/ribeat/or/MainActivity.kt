package com.`fun`.monkeys.ribeat.or

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import com.`fun`.monkeys.ribeat.or.data.Category
import com.`fun`.monkeys.ribeat.or.data.Order
import com.`fun`.monkeys.ribeat.or.data.Product
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo


class MainActivity : AppCompatActivity(),
        CategoryDetailFragment.OnItemSelectListener,
        CategoryListFragment.OnFragmentInteractionListener,
        CartFragment.OnFragmentInteractionListener {

    var lastCategory: Category? = null

    override fun onItemSelect(category: Category?) {
        lastCategory = category
    }

    override fun onFragmentInteraction(uri: Uri) {
    }

    private var ui_hot: TextView? = null
    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        send_order.setOnClickListener {
            Order.Manager.sendOrders(OrderController.INSTANCE.getOrders()).subscribe { _ ->
                OrderController.INSTANCE.resetOrders()
            }.addTo(compositeDisposable)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        val cart = menu.findItem(R.id.cart).actionView
        cart.setOnClickListener {
            val action = when(supportFragmentManager.primaryNavigationFragment!!.childFragmentManager.primaryNavigationFragment) {
                is CategoryListFragment -> CategoryListFragmentDirections.actionCategoryListFragmentToCartFragment()
                is CategoryDetailFragment -> CategoryDetailFragmentDirections.actionCategoryDetailFragmentToCartFragment()
                is CartFragment -> {

                    findNavController(this, R.id.nav_host_fragment).popBackStack()
                    return@setOnClickListener
                }
                else -> {
                    return@setOnClickListener
                }
            }
            findNavController(this, R.id.nav_host_fragment).navigate(action)
        }
        ui_hot = cart.findViewById(R.id.hotlist_hot) as TextView
        startOrderCountUpdate()
        return super.onCreateOptionsMenu(menu)
    }

    private fun startOrderCountUpdate() {
        OrderController.INSTANCE.ordersChanged.subscribe {
            total_cost.text = getString(R.string.lei, it.map { it.quantity * Product.Manager[it.productId]!!.price }.sum())
            if (it.isEmpty())
                ui_hot?.visibility = View.INVISIBLE
            else {
                ui_hot?.visibility = View.VISIBLE
                ui_hot?.text = it.size.toString()
            }
        }.addTo(compositeDisposable)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // action with ID action_refresh was selected
            R.id.cart -> NavHostFragment.findNavController(nav_host_fragment).navigate(R.id.action_categoryListFragment_to_cartFragment)
        }

        return true
    }
}
