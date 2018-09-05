package com.sidbola.cryptrack.features.authentication.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.features.authentication.initialsetup.InitialSetupNavigation
import com.sidbola.cryptrack.features.authentication.initialsetup.USER_PREFERENCES_FILENAME
import com.sidbola.cryptrack.features.mainscreen.MainNavigationActivity
import kotlinx.android.synthetic.main.activity_pin_login.*

const val USER_PIN_CREATED = "user_pin_created"

class PinLoginActivity : AppCompatActivity() {

    private var pin = Array(4) { 0 }
    private var currentPinDigit = 0
    private var correctPin = ""

    private var userPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_login)

        setButtonListeners()

        userPreferences = getSharedPreferences(USER_PREFERENCES_FILENAME, Context.MODE_PRIVATE)

        if (userPreferences?.getBoolean(USER_PIN_CREATED, true) == true) {
            val intent = Intent(this, InitialSetupNavigation::class.java)
            startActivity(intent)
            finish()
        }

        correctPin = userPreferences?.getString("pin", "").toString()
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
                pin[currentPinDigit] = value - '0'
                currentPinDigit++
                updateDigits()
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

                var enteredPin = ""
                for (i in pin) {
                    enteredPin += i.toString()
                }

                val accepted = enteredPin == correctPin
                if (accepted) {
                    val intent = Intent(this, MainNavigationActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    currentPinDigit = 0
                    updateDigits()
                    Snackbar.make(
                            pinLoginHolder,
                            "Incorrect pin. Try again.",
                            Snackbar.LENGTH_LONG).show()
                    val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        // vibrator.vibrate(150)
                    }
                }
            }
        }
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
}
