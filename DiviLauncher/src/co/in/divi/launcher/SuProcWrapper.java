package co.in.divi.launcher;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;

public class SuProcWrapper {

    static final String TAG = SuProcWrapper.class.getName();
    static final String RetCodePrefix = "RetCode: ";
    Process mProcess = null;
    String mSuPath = null;
    boolean mIsUsable = false;

    static class SuProcInAccessibleException extends Exception {
        String mCause = null;

        SuProcInAccessibleException( String cause ) {
            mCause = cause;
        }

        String getReason( ) {
            return mCause;
        }
    }

    SuProcWrapper( String suPath ) throws SuProcInAccessibleException {
        mSuPath = suPath;
        if( suPath != null ) {
            mIsUsable = true;
        }
        throw new SuProcInAccessibleException( "couldn't locate su" );
    }

    boolean isUsable() {
        return mIsUsable;
    }

    boolean isRunning() {

        // todo: this is a hack, needed becuase processbuilder is crap
        // use a thread with waitFor() instead
        Field field = null;
        try {
            field = mProcess.getClass().getDeclaredField("hasExited");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        boolean hasExited = false;
        try {
            hasExited = (Boolean) field.get( mProcess );
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return !hasExited;
    }

    void stop() {
        if( mProcess != null ) {

            if( isRunning() ) {

                OutputStreamWriter cmdInput = new OutputStreamWriter( mProcess.getOutputStream() );
                try {
                    cmdInput.write( "\nexit\n" );
                    mProcess.waitFor();
                } catch ( IOException ex ) {
                    ex.printStackTrace();
                } catch ( InterruptedException ex ) {
                    ex.printStackTrace();
                }
            }

            mProcess.destroy();
            mProcess = null;

        }
    }

    void startIfRequired() {
        if( mProcess == null ) {
            try {
                mProcess = new ProcessBuilder().command( "su", "sh" ).redirectErrorStream( true ).start();
                // run test command, if this fails there is something wrong with su
                int ret = runCommandInternal( "echo" );
                if( ret == -1 ) {
                    mProcess.destroy();
                    mProcess = null;
                }

            } catch( IOException ex ) {
                ex.printStackTrace();
                mProcess.destroy();
                mProcess = null;
            }
        }
    }

    int waitForOutputAndGetRetCode( InputStreamReader out ) {
        BufferedReader reader = new BufferedReader( out );
        String line;
        try {

            while( ( line = reader.readLine() ) != null ) {
                Log.d(TAG, "Recieved Line :" + line );

                if (line.matches(RetCodePrefix + ".*")) {
                    line = line.replace(RetCodePrefix, "");
                    return Integer.parseInt(line);
                }

                if ( !isRunning() )
                    return -1;
            }

        } catch( IOException ex ) {
            ex.printStackTrace();
            return -1;
        }
        return -1; // todo: how can it get here?
    }

    int runCommandInternal( String cmd ) {
        OutputStreamWriter cmdInput = new OutputStreamWriter( mProcess.getOutputStream() );
        try {
            cmdInput.write( cmd + "\n" + "echo \"" + RetCodePrefix + "$?\"\n" );
            InputStreamReader cmdOutput = new InputStreamReader( mProcess.getInputStream() );
            return waitForOutputAndGetRetCode( cmdOutput );
        } catch (IOException e) {
            e.printStackTrace();
            mProcess.destroy();
            mProcess = null;
            return -1;
        }
    }

    int runCommand( String cmd ) throws SuProcInAccessibleException {
        if( !mIsUsable )
            throw new SuProcWrapper.SuProcInAccessibleException( "su not usable" );

        startIfRequired();

        if( mProcess == null ) {
            mIsUsable = false;
            return -1;
        }

        return runCommandInternal(cmd);

    }

}
