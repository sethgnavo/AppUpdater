package com.github.javiersantos.appupdater;

import android.content.Context;
import android.os.AsyncTask;

import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.GitHub;
import com.github.javiersantos.appupdater.objects.Update;

class UtilsAsync {

    static class LatestAppVersion extends AsyncTask<Void, Void, Update> {
        private Context context;
        private LibraryPreferences libraryPreferences;
        private Boolean fromUtils;
        private UpdateFrom updateFrom;
        private GitHub gitHub;
        private String xmlUrl;
        private AppUpdater.LibraryListener listener;

        public LatestAppVersion(Context context, Boolean fromUtils, UpdateFrom updateFrom, GitHub gitHub, String xmlUrl, AppUpdater.LibraryListener listener) {
            this.context = context;
            this.libraryPreferences = new LibraryPreferences(context);
            this.fromUtils = fromUtils;
            this.updateFrom = updateFrom;
            this.gitHub = gitHub;
            this.xmlUrl = xmlUrl;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (UtilsLibrary.isNetworkAvailable(context)) {
                if (!fromUtils && !libraryPreferences.getAppUpdaterShow()) {
                    cancel(true);
                } else {
                    if (updateFrom == UpdateFrom.GITHUB) {
                        if (!GitHub.isGitHubValid(gitHub)) {
                            cancel(true);
                        }
                    }
                }
            } else {
                cancel(true);
            }
        }

        @Override
        protected Update doInBackground(Void... voids) {
            if (updateFrom == UpdateFrom.XML) {
                return UtilsLibrary.getLatestAppVersionXml(xmlUrl);
            } else {
                return UtilsLibrary.getLatestAppVersionHttp(context, updateFrom, gitHub);
            }
        }

        @Override
        protected void onPostExecute(Update update) {
            super.onPostExecute(update);
            if (UtilsLibrary.isStringAVersion(update.getLatestVersion())) {
                listener.onSuccess(update);
            } else {
                listener.onFailed(AppUpdaterError.UPDATE_VARIES_BY_DEVICE);
            }
        }
    }

}
