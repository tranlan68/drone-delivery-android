package com.delivery.core.utils.custom

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.delivery.core.R
import com.delivery.core.databinding.LayoutEdittextInformationCustomBinding
import com.delivery.core.utils.StringUtils.gone
import com.delivery.core.utils.StringUtils.setTextRequired
import com.delivery.core.utils.StringUtils.visible
import com.delivery.core.utils.convertSourceToPixel
import com.delivery.core.utils.hideKeyboard
import com.delivery.core.utils.onDone
import com.delivery.core.utils.setMaxLength
import timber.log.Timber

class EditTextInformationCustom : ConstraintLayout {

    private lateinit var binding: LayoutEdittextInformationCustomBinding

    private var onChangeText: ((String) -> Unit)? = null
    private var onActionDone: ((String) -> Unit)? = null
    private var onFocus: ((String) -> Unit)? = null
    private var isFocus = false
    private var totalLengthUI = 0

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    @SuppressLint("SetTextI18n")
    private fun init(context: Context, attrs: AttributeSet? = null) {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_edittext_information_custom,
            this,
            true
        )

        val typeArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CustomEdittext, 0, 0)
        val stringTitle = typeArray.getResourceId(R.styleable.CustomEdittext_title_edittext, -1)
        val saveEnabled = typeArray.getBoolean(R.styleable.CustomEdittext_save_enabled, false)
        binding.edtInformation.isSaveEnabled = saveEnabled
        if (stringTitle != -1) {
            val valueIsRequired =
                typeArray.getBoolean(R.styleable.CustomEdittext_value_is_required, false)
            if (valueIsRequired) {
                binding.tvTitle.setTextRequired(resources.getString(stringTitle))
            } else {
                binding.tvTitle.text = resources.getString(stringTitle)
            }
        }

        val srcImageTitle = typeArray.getResourceId(R.styleable.CustomEdittext_src_image_title, -1)
        if (srcImageTitle != -1) {
            binding.imvTitle.setImageResource(srcImageTitle)
            binding.edtInformation.setPadding(
                context.convertSourceToPixel(R.dimen.dp_40),
                0,
                context.convertSourceToPixel(R.dimen.dp_40),
                0
            )
        } else {
            binding.imvTitle.gone()
            binding.edtInformation.setPadding(
                context.convertSourceToPixel(R.dimen.dp_20),
                0,
                context.convertSourceToPixel(R.dimen.dp_20),
                0
            )
        }

        val isShowTitle = typeArray.getBoolean(R.styleable.CustomEdittext_is_show_title, true)
        setShowTitle(isShowTitle)

        val isShowCount = typeArray.getBoolean(R.styleable.CustomEdittext_is_show_count, false)
        setShowCount(isShowCount)

        val hintEdittext =
            typeArray.getResourceId(R.styleable.CustomEdittext_text_hint_edittext, -1)
        if (hintEdittext != -1) {
            binding.edtInformation.setHint(hintEdittext)
        }

//        val maxLength = typeArray.getInteger(R.styleable.CustomEdittext_max_length, 0)
//        if (maxLength != 0) {
//            binding.edtInformation.setMaxLength(maxLength)
//        }
        var inputTypeEdittext = typeArray.getResourceId(
            R.styleable.CustomEdittext_type_edittext,
            InputType.TYPE_CLASS_TEXT
        )
        val inputType = typeArray.getResourceId(
            R.styleable.CustomEdittext_android_inputType,
            InputType.TYPE_NULL
        )
        if (inputType > 0) {
            inputTypeEdittext = inputType
        }

        binding.edtInformation.inputType = inputTypeEdittext
        
        // Set unique tag for each instance to help with state management
        binding.edtInformation.tag = "EditTextInformation_${System.currentTimeMillis()}_${hashCode()}"

        binding.imvClearText.setOnClickListener {
            binding.edtInformation.text?.clear()
            binding.imvClearText.isVisible = false
        }

        binding.edtInformation.addTextChangedListener {
            val text = it.toString()
            onChangeText?.invoke(text)
            if (binding.edtInformation.hasFocus()) {
                binding.imvClearText.isVisible = text.isNotEmpty()
            } else {
                binding.imvClearText.isVisible = false
            }

            binding.tvTotalLength.text = "${text.length}/$totalLengthUI"
        }

        binding.edtInformation.setOnFocusChangeListener { _, hasFocus ->
            isFocus = hasFocus
            onFocus?.invoke(binding.edtInformation.text.toString().trim())
            if (hasFocus) {
                binding.imvClearText.isVisible = getTextEditText().isNotEmpty()
            } else {
                binding.imvClearText.isVisible = false
            }
        }

        binding.edtInformation.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                // Xử lý sự kiện khi người dùng nhấn "Done" hoặc "Enter" trên bàn phím
                onActionDone?.invoke(binding.edtInformation.text.toString().trim())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        val maxLength = typeArray.getInteger(R.styleable.CustomEdittext_max_length, 0)
        if (maxLength > 0) {
            binding.edtInformation.setMaxLength(maxLength)
            totalLengthUI = maxLength
            binding.tvTotalLength.text = "0/$totalLengthUI"
            binding.tvTotalLength.visible()
        }

        val lineNumber = typeArray.getInteger(R.styleable.CustomEdittext_line_number, 1)
        if (lineNumber > 1) {
            binding.edtInformation.setLines(lineNumber)
            binding.edtInformation.maxLines = lineNumber
            binding.edtInformation.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
            binding.edtInformation.setPadding(
                context.convertSourceToPixel(20),
                context.convertSourceToPixel(16),
                context.convertSourceToPixel(40),
                context.convertSourceToPixel(16)
            )
            binding.edtInformation.gravity = Gravity.LEFT
        }
    }


    fun setTextError(error: String) {
        binding.tvError.text = error
        binding.tvError.visible()
        binding.edtInformation.setBackgroundResource(R.drawable.bg_edittext_information_error)
    }

    fun goneError() {
        binding.tvError.text = ""
        binding.tvError.gone()
        binding.edtInformation.setBackgroundResource(R.drawable.bg_edittext_information)
    }

    fun handleEdittextOnTextChange(onChangeText: ((String) -> Unit)? = null) {
        this.onChangeText = onChangeText
    }

    fun handleEdittextOnActionDone(onActionDone: ((String) -> Unit)? = null) {
        this.onActionDone = onActionDone
    }

    fun handleEdittextOnFocus(onFocus: ((String) -> Unit)? = null) {
        this.onFocus = onFocus
    }

    fun getTextEditText(): String {
        return binding.edtInformation.text.toString().trim()
    }

    fun setTextEditText(text: String) {
        binding.edtInformation.setText(text)
    }

    fun getIsFocus(): Boolean {
        return isFocus
    }
    fun setClearFocus() {
        binding.edtInformation.clearFocus()
    }


    fun setInputTypeEdittext(type: Int) {
        binding.edtInformation.inputType = type
    }

    fun setMaxLength(maxLength: Int) {
        binding.edtInformation.setMaxLength(maxLength)
    }

    fun setAutoFillOtp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.edtInformation.setAutofillHints("smsOTPCode")
            binding.edtInformation.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES

        }
    }

    private fun setShowTitle(isShow: Boolean) {
        if (isShow) {
            binding.tvTitle.visible()
        } else {
            binding.tvTitle.gone()
        }
    }

    private fun setShowCount(isShow: Boolean) {
        if (isShow) {
            binding.tvTotalLength.visible()
        } else {
            binding.tvTotalLength.gone()
        }
    }

    fun setDigitsEdittext(digits: String) {
        val keyListener = DigitsKeyListener.getInstance(digits)
        binding.edtInformation.keyListener = keyListener
    }

    fun setNotSpaceEdittext() {
        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }
        binding.edtInformation.filters = arrayOf(inputFilter)
    }

    fun setNotSpaceAndMaxLengthEdittext(maxLength: Int) {
        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }
        val lengthFilter = InputFilter.LengthFilter(maxLength)
        binding.edtInformation.filters = arrayOf(inputFilter, lengthFilter)
    }

    fun goneImageViewTitle() {
        binding.imvTitle.gone()
    }

    fun setShowTotalLength(total: Int) {
        totalLengthUI = total
        binding.tvTotalLength.visible()
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    fun setImeOption(action: Int = EditorInfo.IME_ACTION_DONE) {
        binding.edtInformation.imeOptions = action
        binding.edtInformation.isSingleLine = true

        binding.edtInformation.onDone {
            binding.edtInformation.hideKeyboard()
        }
    }

    fun setBackgroundErrorGoneText() {
        binding.tvError.gone()
        if (!isFocus) {
            binding.edtInformation.setBackgroundResource(R.drawable.bg_edittext_information_error)
        }
    }

    fun showClearIconEditText() {
        if (!binding.edtInformation.text.isNullOrEmpty()) {
            binding.imvClearText.visible()
        } else {
            binding.imvClearText.gone()
        }
    }


    fun setInputFilter(filter: Array<InputFilter>) {
        binding.edtInformation.filters = filter
    }

    fun setTitle(title: String, isRequired: Boolean) {
        if (isRequired) {
            binding.tvTitle.setTextRequired(title)
        } else {
            binding.tvTitle.text = title
        }
    }

    fun setTitleNormalFont() {
        binding.tvTitle.setTextAppearance(R.style.text_14)
    }

    fun setEnableEdittext(isEnable: Boolean) {
        binding.edtInformation.isEnabled = isEnable
    }

    fun setImgTitle(srcImageTitle : Int){
        binding.imvTitle.setImageResource(srcImageTitle)
        binding.imvTitle.visible()
    }

    fun setBackgroundEdtInformation(background : Int){
        try {
            binding.edtInformation.setBackgroundResource(background)
        }catch (e : Exception){
            e.printStackTrace()
            Timber.tag("EditTextInformationCustom").e("e : $e")
        }
    }

    fun showImgPhoneBook(status : Boolean){
        binding.imvPhoneBook.isVisible = status
    }

    fun setClickImgPhoneBook(onClick :()->Unit) {
        binding.imvPhoneBook.setOnClickListener {
            onClick()
        }
    }

    fun setImeOptions(imeAction: Int = EditorInfo.IME_ACTION_NEXT) {
        binding.edtInformation.imeOptions = imeAction
    }
    fun setOnEditorActionListener(imeAction: Int,handleAction: (() ->Unit)? = null){
        binding.edtInformation.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == imeAction) {
                handleAction?.invoke()
                true
            } else {
                false
            }
        }

    }
}