package com.sidbola.cryptrack.features.authentication.initialsetup

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sidbola.cryptrack.R
import kotlinx.android.synthetic.main.fragment_pin_creation.*

class PinCreationFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private var userPreferences: SharedPreferences? = null

    private var pin = Array(4) { 0 }
    private var pinVerification = Array(4) { 0 }

    private var originalPin: Boolean = true
    private var currentPinDigit = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_pin_creation, container, false)

        userPreferences = activity?.getSharedPreferences(USER_PREFERENCES_FILENAME, Context.MODE_PRIVATE)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonListeners()
    }

    private fun registerButtonPress(value: Char) {
        when (value) {
            'd' -> {
                currentPinDigit--
                if (currentPinDigit < 0) {
                    currentPinDigit = 0
                }
                updateDigits()
            }
            'c' -> {
                currentPinDigit = 0
                updateDigits()
            }
            else -> {
                if (originalPin) {
                    pin[currentPinDigit] = value - '0'
                    currentPinDigit++
                    updateDigits()
                } else {
                    pinVerification[currentPinDigit] = value - '0'
                    currentPinDigit++
                    updateDigits()
                }
            }
        }
    }

    private fun updateDigits() {
        when (currentPinDigit) {
            0 -> {
                firstDigit.setImageResource(R.drawable.ic_circle_unchecked)
                secondDigit.setImageResource(R.drawable.ic_circle_unchecked)
                thirdDigit.setImageResource(R.drawable.ic_circle_unchecked)
                fourthDigit.setImageResource(R.drawable.ic_circle_unchecked)
            }
            1 -> {
                firstDigit.setImageResource(R.drawable.ic_circle_checked)
                secondDigit.setImageResource(R.drawable.ic_circle_unchecked)
                thirdDigit.setImageResource(R.drawable.ic_circle_unchecked)
                fourthDigit.setImageResource(R.drawable.ic_circle_unchecked)
            }
            2 -> {
                firstDigit.setImageResource(R.drawable.ic_circle_checked)
                secondDigit.setImageResource(R.drawable.ic_circle_checked)
                thirdDigit.setImageResource(R.drawable.ic_circle_unchecked)
                fourthDigit.setImageResource(R.drawable.ic_circle_unchecked)
            }
            3 -> {
                firstDigit.setImageResource(R.drawable.ic_circle_checked)
                secondDigit.setImageResource(R.drawable.ic_circle_checked)
                thirdDigit.setImageResource(R.drawable.ic_circle_checked)
                fourthDigit.setImageResource(R.drawable.ic_circle_unchecked)
            }
            4 -> {
                firstDigit.setImageResource(R.drawable.ic_circle_checked)
                secondDigit.setImageResource(R.drawable.ic_circle_checked)
                thirdDigit.setImageResource(R.drawable.ic_circle_checked)
                fourthDigit.setImageResource(R.drawable.ic_circle_checked)

                if (originalPin) {
                    originalPin = false
                    currentPinDigit = 0
                    updateDigits()
                    mListener?.onPinCreationFragmentInteraction("first_pin_entered")
                } else {
                    val accepted = checkPins()
                    if (accepted) {
                        var pinToStore = ""
                        for (i in pin) {
                            pinToStore += i.toString()
                        }
                        val editor: SharedPreferences.Editor = userPreferences!!.edit()
                        editor.putString("pin", pinToStore)
                        editor.apply()
                        originalPin = true
                        currentPinDigit = 0
                        updateDigits()
                        mListener?.onPinCreationFragmentInteraction("pin_accepted")
                    } else {
                        mListener?.onPinCreationFragmentInteraction("pin_declined")
                        originalPin = true
                        currentPinDigit = 0
                        updateDigits()
                    }
                }
            }
        }
    }

    private fun checkPins(): Boolean {
        return pin contentEquals pinVerification
    }

    private fun setButtonListeners() {
        button0.setOnClickListener { registerButtonPress('0') }
        button1.setOnClickListener { registerButtonPress('1') }
        button2.setOnClickListener { registerButtonPress('2') }
        button3.setOnClickListener { registerButtonPress('3') }
        button4.setOnClickListener { registerButtonPress('4') }
        button5.setOnClickListener { registerButtonPress('5') }
        button6.setOnClickListener { registerButtonPress('6') }
        button7.setOnClickListener { registerButtonPress('7') }
        button8.setOnClickListener { registerButtonPress('8') }
        button9.setOnClickListener { registerButtonPress('9') }
        button0.setOnClickListener { registerButtonPress('0') }
        button_del.setOnClickListener { registerButtonPress('d') }
        button_star.setOnClickListener { registerButtonPress('c') }
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
        fun onPinCreationFragmentInteraction(response: String)
    }
}
