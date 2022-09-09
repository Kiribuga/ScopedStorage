package com.example.scopedstorage.ui.dialogs

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.scopedstorage.R
import com.example.scopedstorage.data.DetailVideo
import com.example.scopedstorage.databinding.DialogSelectDirBinding

class SelectDirDialog(
    private val detailVideo: DetailVideo
) : DialogFragment(R.layout.dialog_select_dir) {

    private val vBinding: DialogSelectDirBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vBinding.addVideoButton.setOnClickListener {
            detailVideo.detailVideoWithDir(
                vBinding.uriVideoET.text.toString()
            )
            dismiss()
        }
        vBinding.cancelButton.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        dialog!!.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}