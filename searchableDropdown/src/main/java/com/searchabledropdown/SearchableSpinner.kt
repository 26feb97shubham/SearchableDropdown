package com.searchabledropdown

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.searchabledropdown.interfaces.AtOnAnimationEnd
import com.searchabledropdown.interfaces.OnItemSelectListener
import com.searchabledropdown.utils.CircularReveal
import com.searchabledropdown.utils.SpinnerRecyclerAdapter

class SearchableSpinner(private val context: Context) : DefaultLifecycleObserver {
    lateinit var onItemSelectListener: OnItemSelectListener
    private lateinit var dialog: AlertDialog

    //private lateinit var clickedView: View
    private lateinit var dialogView: View
    private lateinit var recyclerAdapter: SpinnerRecyclerAdapter


    // Views
    private lateinit var textViewTitle: MaterialTextView
    private lateinit var searchView: SearchView
    private lateinit var buttonClose: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var headLayout: LinearLayout


    var isAnimationAllowed: Boolean = false

    var windowTopBackgroundColor: Int? = null
        @ColorInt get

    var windowTitleTextColor: Int = ContextCompat.getColor(context, android.R.color.white)
        @ColorInt get

    var negativeButtonBackgroundColor: Int? = null
        @ColorInt get

    var negativeButtonTextColor: Int = ContextCompat.getColor(context, android.R.color.white)
        @ColorInt get

    var animationDuration: Int = 420
    var showKeyboardByDefault: Boolean = true
    var spinnerCancelable: Boolean = false
    var windowTitle: String? = null
    var searchQueryHint: String = context.getString(android.R.string.search_go)
    var negativeButtonText: String = context.getString(android.R.string.cancel)
    var dismissSpinnerOnItemClick: Boolean = true
    var highlightSelectedItem: Boolean = true
    var negativeButtonVisibility: SpinnerView = SpinnerView.VISIBLE
    var windowTitleVisibility: SpinnerView = SpinnerView.GONE
    var searchViewVisibility: SpinnerView = SpinnerView.VISIBLE
    var selectedItemPosition: Int = -1
    var selectedItem: String? = null

    @Suppress("unused")
    enum class SpinnerView(val visibility: Int) {
        VISIBLE(View.VISIBLE),
        INVISIBLE(View.INVISIBLE),
        GONE(View.GONE)
    }

    init {
        initLifeCycleObserver()
    }

    fun show() {
        if (getDialogInfo(true)) {
            //clickedView = view
            dialogView = View.inflate(context, R.layout.searchable_spinner, null)
            buttonClose = dialogView.findViewById(R.id.buttonClose)
            recyclerView = dialogView.findViewById(R.id.recyclerView)
            searchView = dialogView.findViewById(R.id.searchView)
            textViewTitle = dialogView.findViewById(R.id.textViewTitle)
            headLayout = dialogView.findViewById(R.id.headLayout)
            val dialogBuilder =
                AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(spinnerCancelable || negativeButtonVisibility != SpinnerView.VISIBLE)

            dialog = dialogBuilder.create()
            dialog.initView()
            initDialogColorScheme()
            dialog.show()
            dialog.initAlertDialogWindow()
        }
    }

    fun dismiss() {
        if (getDialogInfo(false))
            CircularReveal.startReveal(false, dialog, object : AtOnAnimationEnd {
                override fun onAnimationEndListener(isRevealed: Boolean) {
                    toggleKeyBoard(false)
                    if (::recyclerAdapter.isInitialized) recyclerAdapter.filter(null)
                }
            }, animationDuration)
    }

    fun setSpinnerListItems(spinnerList: ArrayList<String>) {
        recyclerAdapter =
            SpinnerRecyclerAdapter(context, spinnerList, object : OnItemSelectListener {
                override fun setOnItemSelectListener(position: Int, selectedString: String) {
                    selectedItemPosition = position
                    selectedItem = selectedString
                    if (dismissSpinnerOnItemClick) dismiss()
                    if (::onItemSelectListener.isInitialized) onItemSelectListener.setOnItemSelectListener(
                        position,
                        selectedString
                    )
                }
            })
    }

    fun setTitleCustomFont(fontPath: String) {
        try {
            val typeface = Typeface.createFromAsset(context.assets, fontPath)
            textViewTitle.typeface = typeface
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setCancelButtonCustomTextSize(sizeInSp: Float) {
        buttonClose.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeInSp)
    }

    fun setCancelButtonCustomFont(fontPath: String) {
        try {
            val typeface = Typeface.createFromAsset(context.assets, fontPath)
            buttonClose.typeface = typeface
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTitleCustomTextSize(sizeInSp: Float) {
        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeInSp)
    }

    fun isAnimationAllowed(isAnimationAllowed: Boolean) {
        this.isAnimationAllowed = isAnimationAllowed
    }

    //Private helper methods
    @Suppress("unused")
    private fun dismissDialogOnDestroy() {
        if (getDialogInfo(false))
            dialog.dismiss()
    }

    private fun initLifeCycleObserver() {
        if (context is AppCompatActivity)
            context.lifecycle.addObserver(this)
        if (context is FragmentActivity)
            context.lifecycle.addObserver(this)
        if (context is Fragment)
            context.lifecycle.addObserver(this)
    }

    private fun getDialogInfo(toShow: Boolean): Boolean {
        return if (toShow) {
            !::dialog.isInitialized || !dialog.isShowing
        } else
            ::dialog.isInitialized && dialog.isShowing && dialog.window != null && dialog.window?.decorView != null && dialog.window?.decorView?.isAttachedToWindow!!
    }

    private fun AlertDialog.initView() {
        setOnShowListener {
            if (isAnimationAllowed) {
                CircularReveal.startReveal(true, this, object : AtOnAnimationEnd {
                    override fun onAnimationEndListener(isRevealed: Boolean) {
                        if (isRevealed) {
                            toggleKeyBoard(showKeyboardByDefault)
                        }
                    }
                }, animationDuration)
            } else {
                toggleKeyBoard(showKeyboardByDefault)
            }

        }

        if (spinnerCancelable || negativeButtonVisibility != SpinnerView.VISIBLE)
            setOnCancelListener {
                if (::recyclerAdapter.isInitialized) recyclerAdapter.filter(
                    null
                )
            }

        dialog.setOnKeyListener { _, keyCode, event ->
            if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                this@SearchableSpinner.dismiss()
            }
            false
        }

        //init WindowTittle
        if (windowTitle != null || windowTitleVisibility.visibility == SearchView.VISIBLE) {
            textViewTitle.visibility = View.VISIBLE
            textViewTitle.text = windowTitle
            textViewTitle.setTextColor(windowTitleTextColor)
        } else textViewTitle.visibility = windowTitleVisibility.visibility

        //init SearchView
        if (searchViewVisibility.visibility == SearchView.VISIBLE) {
            searchView.queryHint = searchQueryHint
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (::recyclerAdapter.isInitialized) recyclerAdapter.filter(query)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (::recyclerAdapter.isInitialized) recyclerAdapter.filter(newText)
                    return false
                }

            })
        } else searchView.visibility = searchViewVisibility.visibility


        //init NegativeButton
        if (negativeButtonVisibility.visibility == SearchView.VISIBLE) {
            buttonClose = dialogView.findViewById(R.id.buttonClose)
            buttonClose.setOnClickListener {
                it.isClickable = false
                this@SearchableSpinner.dismiss()
            }
            buttonClose.text = negativeButtonText
            buttonClose.setTextColor(negativeButtonTextColor)
        } else buttonClose.visibility = negativeButtonVisibility.visibility

        //set Recycler Adapter
        if (::recyclerAdapter.isInitialized) {
            recyclerAdapter.highlightSelectedItem = highlightSelectedItem
            recyclerView.adapter = recyclerAdapter
        }
    }

    private fun initDialogColorScheme() {
        if (windowTopBackgroundColor != null)
            headLayout.background = ColorDrawable(windowTopBackgroundColor!!)
        if (negativeButtonBackgroundColor != null)
            buttonClose.backgroundTintList =
                ColorStateList.valueOf(negativeButtonBackgroundColor!!)
    }

    private fun AlertDialog.initAlertDialogWindow() {
        val colorDrawable = ColorDrawable(Color.TRANSPARENT)
        val insetBackgroundDrawable = InsetDrawable(colorDrawable, 50, 40, 50, 40)
        window?.setBackgroundDrawable(insetBackgroundDrawable)
        window?.attributes?.layoutAnimationParameters
        window?.attributes?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun toggleKeyBoard(showKeyBoard: Boolean) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        if (showKeyboardByDefault && showKeyBoard) {
            searchView.post {
                searchView.requestFocus()
                imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 1)
            }
        } else {
            imm?.toggleSoftInput(0, 0)
        }
    }
}