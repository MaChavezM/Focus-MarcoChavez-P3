package mx.unam.fc.icat.pomodorotimer.view;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import mx.unam.fc.icat.pomodorotimer.R;

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Cargamos las preferencias desde el recurso XML
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
