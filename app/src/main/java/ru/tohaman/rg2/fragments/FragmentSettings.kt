package ru.tohaman.rg2.fragments


import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import org.jetbrains.anko.ctx
import org.jetbrains.anko.support.v4.ctx
import ru.tohaman.rg2.R
import ru.tohaman.rg2.util.getThemeFromSharedPreference


/**
 * A simple [Fragment] subclass.
 * Use the [FragmentSettings.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentSettings : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }


    companion object {

        fun newInstance(): FragmentSettings {
            return FragmentSettings()
        }


    }

}
