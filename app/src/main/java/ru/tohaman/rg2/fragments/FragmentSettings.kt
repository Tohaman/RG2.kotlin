package ru.tohaman.rg2.fragments


import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import ru.tohaman.rg2.R


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
