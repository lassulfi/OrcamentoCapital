package com.assulfisoft.oramentodecapital.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

import com.assulfisoft.oramentodecapital.R;

import java.util.Currency;
import java.util.Set;

import static android.content.SharedPreferences.*;
import static android.os.Build.VERSION.*;
import static android.os.Build.VERSION_CODES.KITKAT;

/**
 * Fragmento para configurar as preferencias
 *
 * Created by LuisDaniel on 23/12/2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements
        OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    //Atributos
    private Toast mToast;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        //Adiciona o arquivo de preferencias em res->xml->pref_menu.xml
        addPreferencesFromResource(R.xml.pref_menu);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();

        //Realiza o setup da ListPreference
        setupListPreference(prefScreen);

        //Percorre todas das opções e define um valor para o resumo
        for(int i = 0; i < count; i++){
            Preference p = prefScreen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)){
                String value = sharedPreferences.getString(p.getKey(),"");
                setPreferenceSummary(p, value);
            }
        }

        //Adiciona um listener para cada PreferenceEditText
        Preference lowRatePreference = findPreference(getString(R.string.low_rate_key));
        lowRatePreference.setOnPreferenceChangeListener(this);
        Preference highRatePreference = findPreference(getString(R.string.high_rate_key));
        highRatePreference.setOnPreferenceChangeListener(this);
        Preference maxIterationPreference = findPreference(getString(R.string.max_iterations_key));
        maxIterationPreference.setOnPreferenceChangeListener(this);
        Preference reqPrecisionPreference = findPreference(getString(R.string.req_precision_key));
        reqPrecisionPreference.setOnPreferenceChangeListener(this);
    }

    private void setupListPreference(PreferenceScreen prefScreen) {

        //Chave de referencia da ListPreference
        String listPrefKey = getString(R.string.pref_currency_key);

        //Recupera a instancia do objeto ListPreference
        ListPreference listPreference = (ListPreference) prefScreen.findPreference(listPrefKey);

        //Preenche a ListPreference com as moedas disponíveis
        if (listPreference != null){
            //Recupera a lista de moedas disponíveis
            if (SDK_INT >= KITKAT) {
                Set<Currency> currencies = Currency.getAvailableCurrencies();

                CharSequence[] entries = new CharSequence[currencies.size()];
                CharSequence[] values = new CharSequence[currencies.size()];

                int i = 0;
                for (Currency currency: currencies){
                    String tempCurrency = String.format("%s\t%s\t%s",currency.getDisplayName(),
                            currency.getSymbol(), currency.toString());
                    if (!tempCurrency.trim().isEmpty()){
                        entries[i] = tempCurrency;
                        values[i] = currency.getSymbol();
                    }
                    i++;
                }

                //Configura a ListPreferences
                listPreference.setEntries(entries);
                listPreference.setDefaultValue(getString(R.string.pref_currency_default_value));
                listPreference.setEntryValues(values);

            } else {
                showToast(getString(R.string.pref_currency_sdk_version_error));
            }
        } else {
            showToast(getString(R.string.pref_currency_invalid_currency));
        }

    }

    /**
     * Método para atualizar o resumo das preferencias
     *
     * @param preference a preferencia a ser atualizada
     * @param value o valor que a preferencia foi atualizada
     */
    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference){
            //Identifica qual o label da preferencia dentro de uma lista foi atualizado
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            //Se existe uma lista de preferencias, atualiza o Summary
            if (prefIndex >= 0){
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if(preference instanceof EditTextPreference){
            //Para EditTextPreferences torna o Summary o valor da caixa de texto
            preference.setSummary(value);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Identifica qual opcao foi atualizada
        Preference preference = findPreference(key);
        if (preference != null){
            //Atualiza o summary da preferencia
            if (!(preference instanceof CheckBoxPreference)){
                String value = sharedPreferences.getString(preference.getKey(),"");
                setPreferenceSummary(preference, value);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //low rate key
        String lowRateKey = getString(R.string.low_rate_key);
        //high rate key
        String highRateKey = getString(R.string.high_rate_key);
        //max iterations
        String maxIterationsKey = getString(R.string.max_iterations_key);
        //req precision key
        String reqPrecisionKey = getString(R.string.req_precision_key);

        //Recupera a chave que foi alterada nas preferencias
        String preferenceKey = preference.getKey();

        if (preferenceKey.equals(lowRateKey)){
            String stringSize = ((String) (newValue)).trim();
            if (stringSize.equals("")){
                stringSize = "1";
            }
            try {
                double size = Double.parseDouble(stringSize);
                if (size > 0.2 || size <= 0){
                    showToast(getString(R.string.pref_low_rate_toast_message));
                    return false;
                }
            } catch (NumberFormatException nef){
                showToast(getString(R.string.pref_low_rate_toast_message));
                return false;
            }
        } else if(preferenceKey.equals(highRateKey)){
            String stringSize = ((String) (newValue)).trim();
            if (stringSize.equals("")){
                stringSize = "1";
            }
            try {
                double size = Double.parseDouble(stringSize);
                if (size > 1.0 || size <= 0.4){
                    showToast(getString(R.string.pref_high_rate_toast_message));
                    return false;
                }
            } catch (NumberFormatException nef){
                showToast(getString(R.string.pref_high_rate_toast_message));
                return false;
            }
        } else if(preferenceKey.equals(maxIterationsKey)){
            String stringSize = ((String) (newValue)).trim();
            if (stringSize.equals("")){
                stringSize = "1";
            }
            try {
                int size = Integer.parseInt(stringSize);
                if (size > 10000 || size <= 1){
                    showToast(getString(R.string.pref_max_iterations_toast_message));
                    return false;
                }
            } catch (NumberFormatException nef){
                showToast(getString(R.string.pref_max_iterations_toast_message));
                return false;
            }
        } else if (preferenceKey.equals(reqPrecisionKey)){
            String stringSize = ((String) (newValue)).trim();
            if (stringSize.equals("")){
                stringSize = "1";
            }
            try {
                double size = Double.parseDouble(stringSize);;
                if (size > 0.1 || size <= 1E-15){
                    showToast(getString(R.string.pref_max_iterations_toast_message));
                    return false;
                }
            } catch (NumberFormatException nef){
                showToast(getString(R.string.pref_max_iterations_toast_message));
                return false;
            }
        }
        return true;
    }

    /**
     * Método para criar um Toast a partir de uma string
     * @param toastMessage String contendo a mensagem de Toast
     * @return objeto Toast
     */
    private void showToast(String toastMessage){
        if (mToast != null){
            mToast.cancel();
        }
        mToast =Toast.makeText(getContext(),toastMessage,Toast.LENGTH_SHORT);
        mToast.show();
    }
}
