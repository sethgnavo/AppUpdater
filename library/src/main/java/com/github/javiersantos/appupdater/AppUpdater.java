package com.github.javiersantos.appupdater;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.Duration;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.GitHub;

public class AppUpdater {
    private Context context;
    private LibraryPreferences libraryPreferences;
    private Display display;
    private UpdateFrom updateFrom;
    private Duration duration;
    private GitHub gitHub;
    private Integer showEvery;
    private Boolean showAppUpdated;
    private String titleUpdate, descriptionUpdate, btnUpdate, btnDisable; // Update available
    private String titleNoUpdate, descriptionNoUpdate; // Update not available

    public AppUpdater(Context context) {
        this.context  = context;
        this.libraryPreferences = new LibraryPreferences(context);
        this.display = Display.DIALOG;
        this.updateFrom = UpdateFrom.GOOGLE_PLAY;
        this.duration = Duration.NORMAL;
        this.showEvery = 1;
        this.showAppUpdated = false;

        // Dialog
        this.titleUpdate = context.getResources().getString(R.string.appupdater_update_available);
        this.titleNoUpdate = context.getResources().getString(R.string.appupdater_update_not_available);
        this.btnUpdate = context.getResources().getString(R.string.appupdater_btn_update);
        this.btnDisable = context.getResources().getString(R.string.appupdater_btn_disable);
    }

    /**
     * Set the type of message used to notify the user when a new update has been found. Default: DIALOG.
     *
     * @param display how the update will be shown
     * @return this
     * @see com.github.javiersantos.appupdater.enums.Display
     */
    public AppUpdater setDisplay(Display display) {
        this.display = display;
        return this;
    }

    /**
     * Set the source where the latest update can be found. Default: GOOGLE_PLAY.
     *
     * @param updateFrom source where the latest update is uploaded. If GITHUB is selected, .setGitHubAndRepo method is required.
     * @return this
     * @see com.github.javiersantos.appupdater.enums.UpdateFrom
     */
    public AppUpdater setUpdateFrom(UpdateFrom updateFrom) {
        this.updateFrom = updateFrom;
        return this;
    }

    /**
     * Set the duration of the Snackbar Default: NORMAL.
     *
     * @param duration duration of the Snackbar
     * @return this
     * @see com.github.javiersantos.appupdater.enums.Duration
     */
    public AppUpdater setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Set the user and repo where the releases are uploaded. You must upload your updates as a release in order to work properly tagging them as vX.X.X or X.X.X.
     *
     * @param user GitHub user
     * @param repo GitHub repository
     * @return this
     */
    public AppUpdater setGitHubUserAndRepo(@NonNull String user, @NonNull String repo) {
        this.gitHub = new GitHub(user, repo);
        return this;
    }

    /**
     * Set the times the app ascertains that a new update is available and display a dialog, Snackbar or notification. It makes the updates less invasive. Default: 1.
     *
     * @param times every X times
     * @return this
     */
    public AppUpdater showEvery(Integer times) {
        this.showEvery = times;
        return this;
    }

    /**
     * Set if the dialog, Snackbar or notification is displayed although there aren't updates. Default: false.
     *
     * @param res true to show, false otherwise
     * @return this
     */
    public AppUpdater showAppUpdated(Boolean res) {
        this.showAppUpdated = res;
        return this;
    }

    /**
     * Set a custom title for the dialog when an update is available.
     *
     * @param title for the dialog
     * @return this
     */
    public AppUpdater setDialogTitleWhenUpdateAvailable(@NonNull String title) {
        this.titleUpdate = title;
        return this;
    }

    /**
     * Set a custom description for the dialog when an update is available.
     *
     * @param description for the dialog
     * @return this
     */
    public AppUpdater setDialogDescriptionWhenUpdateAvailable(@NonNull String description) {
        this.descriptionUpdate = description;
        return this;
    }

    /**
     * Set a custom title for the dialog when no update is available.
     *
     * @param title for the dialog
     * @return this
     */
    public AppUpdater setDialogTitleWhenUpdateNotAvailable(@NonNull String title) {
        this.titleNoUpdate = title;
        return this;
    }

    /**
     * Set a custom description for the dialog when no update is available.
     *
     * @param description for the dialog
     * @return this
     */
    public AppUpdater setDialogDescriptionWhenUpdateNotAvailable(@NonNull String description) {
        this.descriptionNoUpdate = description;
        return this;
    }

    /**
     * Set a custom "Update" button text when a new update is available.
     *
     * @param text for the update button
     * @return this
     */
    public AppUpdater setDialogButtonUpdate(@NonNull String text) {
        this.btnUpdate = text;
        return this;
    }

    /**
     * Set a custom "Don't show again" button text when a new update is available.
     *
     * @param text for the disable button
     * @return this
     */
    public AppUpdater setDialogButtonDoNotShowAgain(@NonNull String text) {
        this.btnDisable = text;
        return this;
    }

    /**
     * Execute AppUpdater in background.
     *
     * @return this
     * @deprecated use {@link #start()} instead
     */
    public AppUpdater init() {
        start();
        return this;
    }

    /**
     * Execute AppUpdater in background.
     */
    public void start() {
        UtilsAsync.LatestAppVersion latestAppVersion = new UtilsAsync.LatestAppVersion(context, false, updateFrom, gitHub, new LibraryListener() {
            @Override
            public void onSuccess(String version) {
                if (UtilsLibrary.isUpdateAvailable(UtilsLibrary.getAppInstalledVersion(context), version)) {
                    Integer successfulChecks = libraryPreferences.getSuccessfulChecks();
                    if (UtilsLibrary.isAbleToShow(successfulChecks, showEvery)) {
                        switch (display) {
                            case DIALOG:
                                UtilsDisplay.showUpdateAvailableDialog(context, titleUpdate, getDescriptionUpdate(context, version), btnUpdate, btnDisable, updateFrom, gitHub);
                                break;
                            case SNACKBAR:
                                UtilsDisplay.showUpdateAvailableSnackbar(context, String.format(context.getResources().getString(R.string.appupdater_update_available_description_snackbar), version), UtilsLibrary.getDurationEnumToBoolean(duration), updateFrom, gitHub);
                                break;
                            case NOTIFICATION:
                                UtilsDisplay.showUpdateAvailableNotification(context, context.getResources().getString(R.string.appupdater_update_available), String.format(context.getResources().getString(R.string.appupdater_update_available_description_notification), version, UtilsLibrary.getAppName(context)), updateFrom, gitHub);
                                break;
                        }
                    }
                    libraryPreferences.setSuccessfulChecks(successfulChecks + 1);
                } else if (showAppUpdated) {
                    switch (display) {
                        case DIALOG:
                            UtilsDisplay.showUpdateNotAvailableDialog(context, titleNoUpdate, getDescriptionNoUpdate(context));
                            break;
                        case SNACKBAR:
                            UtilsDisplay.showUpdateNotAvailableSnackbar(context, context.getResources().getString(R.string.appupdater_update_not_available_description), UtilsLibrary.getDurationEnumToBoolean(duration));
                            break;
                        case NOTIFICATION:
                            UtilsDisplay.showUpdateNotAvailableNotification(context, context.getResources().getString(R.string.appupdater_update_not_available), String.format(context.getResources().getString(R.string.appupdater_update_not_available_description), UtilsLibrary.getAppName(context)));
                            break;
                    }
                }
            }
        });

        latestAppVersion.execute();
    }

    interface LibraryListener {
        void onSuccess(String version);
    }

    private String getDescriptionUpdate(Context context, String version) {
        if (descriptionUpdate == null) {
            return String.format(context.getResources().getString(R.string.appupdater_update_available_description_dialog), version, UtilsLibrary.getAppName(context));
        } else {
            return descriptionUpdate;
        }
    }

    private String getDescriptionNoUpdate(Context context) {
        if (descriptionNoUpdate == null) {
            return String.format(context.getResources().getString(R.string.appupdater_update_not_available_description), UtilsLibrary.getAppName(context));
        } else {
            return descriptionNoUpdate;
        }
    }

}
