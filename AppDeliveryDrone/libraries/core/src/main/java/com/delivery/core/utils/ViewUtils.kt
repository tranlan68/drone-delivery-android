package com.delivery.core.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.text.Editable
import android.text.InputFilter
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.delivery.core.R
import com.delivery.core.utils.Constants.DURATION_TIME_CLICKABLE
import com.delivery.core.utils.ViewUtils.lastClick

object ViewUtils {
    // check double click
    @JvmStatic
    fun runLayoutAnimation(
        recyclerView: RecyclerView,
        @AnimRes resId: Int,
    ) {
        val context = recyclerView.context
        val controller =
            AnimationUtils.loadLayoutAnimation(context, resId)
        recyclerView.layoutAnimation = controller
        recyclerView.scheduleLayoutAnimation()
    }

    var lastClick = 0L
}

fun String.toast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
fun TextView.disableCopyPaste() {
    isLongClickable = false
    setTextIsSelectable(false)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        customSelectionActionModeCallback =
            object : ActionMode.Callback {
                override fun onCreateActionMode(
                    mode: ActionMode?,
                    menu: Menu,
                ): Boolean {
                    return false
                }

                override fun onPrepareActionMode(
                    mode: ActionMode?,
                    menu: Menu,
                ): Boolean {
                    return false
                }

                override fun onActionItemClicked(
                    mode: ActionMode?,
                    item: MenuItem,
                ): Boolean {
                    return false
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                    // do nothing
                }
            }
    }
}

@RequiresApi(Build.VERSION_CODES.FROYO)
fun ImageView.enableView(isEnable: Boolean) {
    isEnabled =
        if (isEnable) {
            setColorFilter(context.getColorCompat(R.color.color_button_common_blue))
            true
        } else {
            setColorFilter(context.getColorCompat(R.color.background_color_gray))
            false
        }
}

fun ImageView.tint(
    @ColorRes colorId: Int,
) {
    setColorFilter(context.getColorCompat(colorId))
}

fun EditText.onTextChange(content: (Editable?) -> Unit) {
    addTextChangedListener(
        object : android.text.TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int,
            ) {
                // do nothing
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int,
            ) {
                // do nothing
            }

            override fun afterTextChanged(s: Editable?) {
                content(s)
            }
        },
    )
}

fun ViewPager.onPageSelected(params: (Int) -> Unit) {
    addOnPageChangeListener(
        object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                // do nothing
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                // do nothing
            }

            override fun onPageSelected(position: Int) {
                params(position)
            }
        },
    )
}

/*fun TextView.setTextAsync(data: String) {
    TextViewCompat.setPrecomputedText(
        this,
        PrecomputedTextCompat.create(data, TextViewCompat.getTextMetricsParams(this))
    )
}*/

fun Activity.toastMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.toastMessage(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun View.setOnSafeClickListener(
    duration: Long = DURATION_TIME_CLICKABLE,
    onClick: () -> Unit,
) {
    setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClick >= duration) {
            onClick()
            lastClick = SystemClock.elapsedRealtime()
        }
    }
}

fun ViewPager.getCurrentFragment(fragmentManager: FragmentManager): Any? {
    return getFragmentAt(fragmentManager, currentItem)
}

fun ViewPager.getFragmentAt(
    fragmentManager: FragmentManager,
    index: Int,
): Any? {
    return fragmentManager.findFragmentByTag("android:switcher:$id:$index")
        ?: return adapter?.instantiateItem(this, index)
}

fun ViewPager2.getCurrentFragment(fragmentManager: FragmentManager): Fragment? {
    return getFragmentAt(fragmentManager, currentItem)
}

fun ViewPager2.getFragmentAt(
    fragmentManager: FragmentManager,
    index: Int,
): Fragment? {
    return fragmentManager.findFragmentByTag("f$index")
}

fun EditText.setMaxLength(maxLength: Int) {
    val fArray = arrayOfNulls<InputFilter>(1)
    fArray[0] = InputFilter.LengthFilter(maxLength)
    this.filters = fArray
}

@RequiresApi(Build.VERSION_CODES.CUPCAKE)
fun EditText.onDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

@RequiresApi(Build.VERSION_CODES.CUPCAKE)
fun EditText.showKeyBoard() {
    if (this.requestFocus()) {
        val inputMethodManager: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

@RequiresApi(Build.VERSION_CODES.CUPCAKE)
fun EditText.hideKeyBoard() {
    if (this.requestFocus()) {
        val inputMethodManager: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

@RequiresApi(Build.VERSION_CODES.CUPCAKE)
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}
