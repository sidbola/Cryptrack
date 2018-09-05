package com.sidbola.cryptrack.features.authentication.initialsetup

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sidbola.cryptrack.R
import kotlinx.android.synthetic.main.fragment_pin_creation_completion.*

class CompletionFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pin_creation_completion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        finishSetupButton.setOnClickListener { mListener?.onCompletionFragmentInteraction("setup_complete") }

        status.text = getString(R.string.fetchingData)

        status.text = getString(R.string.finishedSetup)
        finishSetupButton.isEnabled = true
        finishSetupButton.text = getString(R.string.beginUse)
        dbInitializeProgress.visibility = View.GONE

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        fun onCompletionFragmentInteraction(response: String)
    }
}
