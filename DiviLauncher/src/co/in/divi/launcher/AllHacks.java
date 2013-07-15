package co.in.divi.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class AllHacks extends HackTask {

    private static final String TAG = AllHacks.class.getName();
    ArrayList< HackTask > mHacks = null;
    Context mContext = null;

    String locateOldSu() {

        // todo: ignore for now
        return "su";

        // todo: check if the directories are listable as the app user
        /*
        // go through the path and locate the old su and save location in preferences
        String ret = mPrefs.getString( "oldSuPath", null );
        if( ret != null )
            return ret;

        String path = System.getenv( "PATH" );
        String[] toks = path.split( ":" );
        for( String tok : toks ) {
            File f = new File( tok + "/su" );
            Log.d( TAG, "Looking for su in " + tok );
            if( f.exists() ) {
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString( "oldSuPath", tok + "/su" );
                editor.commit();
                return tok + "/su";
            }
        }
        return null;
        */
    }

    void populateAllHacks() throws SuProcWrapper.SuProcInAccessibleException {

        if( mHacks == null ) {
            mHacks = new ArrayList< HackTask >();
            final String oldSuPath = locateOldSu();
            final String appFilesDirPath = mContext.getFilesDir().getPath();
            final String jailPath = appFilesDirPath + "/jail";
            final String systemAppPath = "/system/app/";
            final SuProcWrapper suproc = new SuProcWrapper( oldSuPath );
            final String internalSu = appFilesDirPath + "/isu";
/*
            mHacks.add( new HackTask( mPrefs, "CreateJail" ) {
                @Override
                boolean hack() {

                    File jailDir = mContext.getDir( "jail", Context.MODE_PRIVATE );
                    return true;
                }
            });

            mHacks.add( new HackTask( mPrefs, "MakeInternalSuUsable" ) {
                @Override
                boolean hack() {
                    // check if setuid flag is already set
                    File internalSuFile = new File( internalSu );
                    //internalSuFile.

                    // else use su to set it

                    suproc.runCommand( "chmod +s " + internalSu );

                    return true;
                }
                // no undo for this one
            } );

            mHacks.add( new HackTask( mPrefs, "MountRW" ) {
                @Override
                boolean hack() {
                    suproc.runCommand( "mount -o remount,rw /system" );
                    return true;
                }

                @Override
                boolean undo() {
                    suproc.runCommand( "mount -o remount,ro /system" );
                    return true;
                }
            } );

            mHacks.add( new HackTask( mPrefs, "HideOldSu" ) {
                @Override
                boolean hack() {
                    suproc.runCommand( "mv " + oldSuPath + " " + jailPath );
                    return true;
                }

                @Override
                boolean undo() {
                    suproc.runCommand( "mv " + jailPath + "/su " + oldSuPath  );
                    return true;
                }
            } );

            mHacks.add( new HackTask( mPrefs, "HideSystemUI" ) {
                @Override
                boolean hack() {
                    suproc.runCommand( "mv " + systemAppPath + "SystemUI.apk " + jailPath );
                    suproc.runCommand( "mv " + systemAppPath + "SystemUI.odex " + jailPath );
                    return true;
                }

                @Override
                boolean undo() {
                    suproc.runCommand( "mv " + jailPath + "SystemUI.apk " + systemAppPath );
                    suproc.runCommand( "mv " + jailPath + "SystemUI.odex " + systemAppPath );
                    return true;
                }
            } );

            mHacks.add( new HackTask( mPrefs, "HideLauncher" ) {
                @Override
                boolean hack() {
                    suproc.runCommand( "mv " + systemAppPath + "Launcher.apk " + jailPath );
                    suproc.runCommand( "mv " + systemAppPath + "Launcher.odex " + jailPath );
                    return true;
                }

                @Override
                boolean undo() {
                    suproc.runCommand( "mv " + jailPath + "Launcher.apk " + systemAppPath );
                    suproc.runCommand( "mv " + jailPath + "Launcher.odex " + systemAppPath );
                    return true;
                }
            } );

            mHacks.add( new HackTask( mPrefs, "HideAdb" ) {
                @Override
                boolean hack() {
                    // most clueless award for this task
                    return true;
                }
            } );

            mHacks.add( new HackTask( mPrefs, "MountRO" ) {
                @Override
                boolean hack() {
                    suproc.runCommand( "mount -o remount,ro /system" );
                    return true;
                }
                @Override
                boolean undo() {
                    suproc.runCommand( "mount -o remount,rw /system" );
                    return true;
                }
            } );
            */
            mHacks.add( new HackTask( mPrefs, "Test" ) {
                @Override
                boolean hack() throws SuProcWrapper.SuProcInAccessibleException {
                    suproc.runCommand( "id" );
                    return true;
                }
            });
        }
    }

    @Override
    boolean hack() throws SuProcWrapper.SuProcInAccessibleException {
        for( HackTask hack : mHacks) {
            if( !hack.runHack() )
                return false;
        }
        return true;
    }

    @Override
    boolean undo() {

        ArrayList< HackTask > undoList = new ArrayList< HackTask >(mHacks);
        Collections.reverse( undoList );

        for( HackTask hack : undoList ) {
            if( !undoHack() )
                return false;
        }
        return true;
    }

    AllHacks( SharedPreferences prefs, Context context ) throws SuProcWrapper.SuProcInAccessibleException {
        super( prefs, "AllHacks" );
        mContext = context;
        populateAllHacks();
    }

}
