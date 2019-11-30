package com.prng.imagefilepicker;

import androidx.fragment.app.FragmentActivity;

import com.prng.imagefilepicker.callback.FileLoaderCallbacks;
import com.prng.imagefilepicker.callback.FilterResultCallback;
import com.prng.imagefilepicker.entity.NormalFile;

import static com.prng.imagefilepicker.callback.FileLoaderCallbacks.TYPE_FILE;

/**
 * Created by Vincent Woo
 * Date: 2016/10/11
 * Time: 10:19
 */

public class FileFilter {
    public static void getFiles(FragmentActivity activity, FilterResultCallback<NormalFile> callback, String[] suffix) {
        activity.getSupportLoaderManager().initLoader(3, null, new FileLoaderCallbacks(activity, callback, TYPE_FILE, suffix));
    }
}
