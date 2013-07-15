package co.in.divi.launcher;

import android.content.SharedPreferences;

public abstract class HackTask {

    String mName = null;
    SharedPreferences mPrefs = null;

    boolean hack() throws SuProcWrapper.SuProcInAccessibleException {
        return true;
    }

    boolean undo() {
        return true;
    }

    boolean runHack() throws SuProcWrapper.SuProcInAccessibleException {

        // todo: don't use prefs to check, use individual custom checks

        if( mPrefs.getBoolean( mName, false ) )
            return true;

        if( hack() ) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(mName, true);
            editor.commit();
            return true;
        } else
            return false;
    }


    boolean undoHack() {
        if( !mPrefs.getBoolean( mName, false ) )
            return true;
        if( undo() ) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(mName, false);
            editor.commit();
            return true;
        } else
            return false;
    }

    HackTask( SharedPreferences prefs, String name ) {
        mPrefs = prefs;
        mName = name;
    }

}
