package com.sidbola.cryptrack.features.authentication.initialsetup

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.features.mainscreen.MainNavigationActivity
import com.sidbola.cryptrack.features.mainscreen.discover.INITIAL_WATCHLIST_LOADED
import kotlinx.android.synthetic.main.activity_initial_setup_navigation.*

const val USER_PREFERENCES_FILENAME = "com.sidbola.cryptrack.prefs"
const val USER_PIN_CREATED = "user_pin_created"

const val DECREMENT_PROGRESS_DOTS = 0
const val INCREMENT_PROGRESS_DOTS = 1

const val WELCOME_SCREEN_FRAGMENT = 1
const val PIN_CREATION_FRAGMENT = 2
const val COMPLETION_FRAGMENT = 3

class InitialSetupNavigation : AppCompatActivity(),
        WelcomeScreenFragment.OnFragmentInteractionListener,
        PinCreationFragment.OnFragmentInteractionListener,
        CompletionFragment.OnFragmentInteractionListener {

    private var currentStep = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup_navigation)

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.welcomeScreenFragmentHolder, WelcomeScreenFragment())
        ft.commit()
    }

    private fun updateProgressDots(updater: Int) {
        if (updater > 0) {
            currentStep++
        } else {
            currentStep--
        }
        when (currentStep) {
            0 -> {
                secondPageMarker.setImageResource(R.drawable.ic_uncheck_mark)
                thirdPageMarker.setImageResource(R.drawable.ic_uncheck_mark)
                fourthPageMarker.setImageResource(R.drawable.ic_uncheck_mark)
            }
            1 -> {
                secondPageMarker.setImageResource(R.drawable.ic_check_mark)
                thirdPageMarker.setImageResource(R.drawable.ic_uncheck_mark)
                fourthPageMarker.setImageResource(R.drawable.ic_uncheck_mark)
            }
            2 -> {
                secondPageMarker.setImageResource(R.drawable.ic_check_mark)
                thirdPageMarker.setImageResource(R.drawable.ic_check_mark)
                fourthPageMarker.setImageResource(R.drawable.ic_uncheck_mark)
            }
            3 -> {
                secondPageMarker.setImageResource(R.drawable.ic_check_mark)
                thirdPageMarker.setImageResource(R.drawable.ic_check_mark)
                fourthPageMarker.setImageResource(R.drawable.ic_check_mark)
            }
        }
    }

    private fun changeFragment(fragmentToShow: Int) {
        var fragment: Fragment = WelcomeScreenFragment()

        when (fragmentToShow) {
            WELCOME_SCREEN_FRAGMENT -> fragment = WelcomeScreenFragment()
            PIN_CREATION_FRAGMENT -> fragment = PinCreationFragment()
            COMPLETION_FRAGMENT -> fragment = CompletionFragment()
        }

        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right)
        ft.replace(R.id.welcomeScreenFragmentHolder, fragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        updateProgressDots(DECREMENT_PROGRESS_DOTS)
    }

    override fun onWelcomeScreenFragmentInteraction(response: String) {
        changeFragment(PIN_CREATION_FRAGMENT)
        updateProgressDots(INCREMENT_PROGRESS_DOTS)
    }

    override fun onPinCreationFragmentInteraction(response: String) {
        when (response) {
            "pin_accepted" -> {
                Snackbar.make(
                        initialSetupNavigationFrame,
                        "Pin created successfully",
                        Snackbar.LENGTH_LONG).show()
                changeFragment(COMPLETION_FRAGMENT)
                updateProgressDots(INCREMENT_PROGRESS_DOTS)
            }
            "pin_declined" -> {
                Snackbar.make(
                        initialSetupNavigationFrame,
                        "Pins did not match. Try again.",
                        Snackbar.LENGTH_LONG).show()
                updateProgressDots(DECREMENT_PROGRESS_DOTS)
                val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    // vibrator.vibrate(150)
                }
            }
            "first_pin_entered" -> {
                updateProgressDots(INCREMENT_PROGRESS_DOTS)
            }
        }
    }

    override fun onCompletionFragmentInteraction(response: String) {

        val userPreferences = getSharedPreferences(USER_PREFERENCES_FILENAME, Context.MODE_PRIVATE)

        val editor: SharedPreferences.Editor = userPreferences!!.edit()
        editor.putBoolean(USER_PIN_CREATED, false)
        editor.putBoolean(INITIAL_WATCHLIST_LOADED, false)
        editor.apply()



        val intent = Intent(this, MainNavigationActivity::class.java)
        startActivity(intent)
        finish()
    }
}
