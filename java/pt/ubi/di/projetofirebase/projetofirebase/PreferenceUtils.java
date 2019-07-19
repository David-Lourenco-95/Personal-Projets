package pt.ubi.di.projetofirebase.projetofirebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

// Classe que contém as funções das Share Prefences
public class PreferenceUtils {

    public PreferenceUtils(){

    }

    // Função que permite guardar o email do utilizador, quando efetua login
    public static boolean saveEmail(String email, Context context){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(Constants.KEY_EMAIL,email);
        prefsEditor.apply();
        return true;
    }

    // Função que permite obter o email guardado
    public static String getEmail2(Context context){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(Constants.KEY_EMAIL,null);
    }

}
