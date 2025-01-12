/*
 * Copyright (C) 2018 Shobhit Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.dao.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormInfo;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.utilities.MediaUtils;

public final class ContentResolverHelper {

    private ContentResolverHelper() {

    }

    private static ContentResolver getContentResolver() {
        return Collect.getInstance().getContentResolver();
    }

    public static FormInfo getFormDetails(Uri uri) {
        FormInfo formInfo = null;

        try (Cursor instanceCursor = getContentResolver().query(uri, null, null, null, null)) {
            if (instanceCursor != null && instanceCursor.getCount() > 0) {
                instanceCursor.moveToFirst();
                String instancePath = new StoragePathProvider().getAbsoluteInstanceFilePath(instanceCursor
                        .getString(instanceCursor
                                .getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH)));

                String jrFormId = instanceCursor
                        .getString(instanceCursor
                                .getColumnIndex(InstanceColumns.JR_FORM_ID));
                int idxJrVersion = instanceCursor
                        .getColumnIndex(InstanceColumns.JR_VERSION);

                String jrVersion = instanceCursor.isNull(idxJrVersion) ? null
                        : instanceCursor
                        .getString(idxJrVersion);
                formInfo = new FormInfo(instancePath, jrFormId, jrVersion);
            }
        }
        return formInfo;
    }

    public static String getFormPath(Uri uri) {
        String formPath = null;
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.getCount() == 1) {
                c.moveToFirst();
                formPath = new StoragePathProvider().getAbsoluteFormFilePath(c.getString(c.getColumnIndex(FormsColumns.FORM_FILE_PATH)));
            }
        }
        return formPath;
    }

    /**
     * Using contentResolver to get a file's extension by the uri
     *
     * @param fileUri Whose name we want to get
     * @return The file's extension without a dot eg. "mp3" not ".mp3"
     */
    public static String getFileExtensionFromUri(Context context, Uri fileUri) {
        String fileName = MediaUtils.getFileNameFromUri(context, fileUri);
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.') + 1);
        } else {
            return fileUri.getScheme() != null && fileUri.getScheme().equals(ContentResolver.SCHEME_CONTENT)
                    ? MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(fileUri))
                    : MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
        }
    }
}
