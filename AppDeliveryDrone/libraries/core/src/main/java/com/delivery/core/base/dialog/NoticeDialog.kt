package com.delivery.core.base.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.delivery.core.R

const val NOTICE_DIALOG_FRAGMENT = "NOTICE_DIALOG_FRAGMENT"

class NoticeDialog : BaseDialogFragment() {
    var dialogListener: NoticeDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment != null && parentFragment is NoticeDialogListener) {
            dialogListener = parentFragment as NoticeDialogListener
            return
        }

        if (context is NoticeDialogListener) {
            dialogListener = context
            return
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(NOTICE_DIALOG_TITLE) ?: ""
        val type = arguments?.getInt(NOTICE_DIALOG_TYPE)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setPositiveButton(R.string.ok) { _, _ ->
                dialogListener?.onClickOk(type)
            }
        return builder.create()
    }

    companion object {
        private const val NOTICE_DIALOG_TITLE = "NOTICE_DIALOG_TITLE"
        private const val NOTICE_DIALOG_TYPE = "NOTICE_DIALOG_TYPE"

        fun getInstance(
            title: String,
            type: Int? = null,
        ): NoticeDialog {
            val bundle = Bundle()
            bundle.putString(NOTICE_DIALOG_TITLE, title)
            type?.let {
                bundle.putInt(NOTICE_DIALOG_TYPE, type)
            }
            return NoticeDialog().apply { arguments = bundle }
        }
    }

    override fun onDetach() {
        super.onDetach()
        dialogListener = null
    }
}

interface NoticeDialogListener : BaseDialogListener {
    fun onClickOk(type: Int?)
}
