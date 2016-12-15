package com.thinktanki.atmfinder;

import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by aruns512 on 12/12/2016.
 */
public class Helper {
    private String response;

    public String readATMData(InputStream stream) {

        try {
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();
            Reader in = new InputStreamReader(stream, "UTF-8");
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }

            response = out.toString();
        } catch (Exception e) {
            Log.e("HELPER_CLASS", e.getMessage());
        }
        return response;
    }
}
