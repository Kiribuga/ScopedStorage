package com.example.scopedstorage.ui.dialogs

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.scopedstorage.R
import com.example.scopedstorage.data.DetailVideo
import com.example.scopedstorage.databinding.DialogFragmentAddVideoBinding

class AddFragmentDialog(
    private val detailVideo: DetailVideo
) : DialogFragment(R.layout.dialog_fragment_add_video) {

    private val vBinding: DialogFragmentAddVideoBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vBinding.addVideoButton.setOnClickListener {
            detailVideo.detailVideo(
                vBinding.titleVideoET.text.toString(),
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