package com.peihua.miracast;


import android.content.Context;
import android.view.Display;

import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouter.RouteInfo;

/**
 * Abstraction of common playback operations of media items, such as play,
 * seek, etc. Used by PlaybackManager as a backend to handle actual playback
 * of media items.
 */
public abstract class Player {
    protected Callback mCallback;

    public abstract boolean isRemotePlayback();
    public abstract boolean isQueuingSupported();

    public abstract void connect(RouteInfo route);
    public abstract void release();

    // basic operations that are always supported
    public abstract void play(final PlaylistItem item);
    public abstract void seek(final PlaylistItem item);
    public abstract void getStatus(final PlaylistItem item, final boolean update);
    public abstract void pause();
    public abstract void resume();
    public abstract void stop();

    // advanced queuing (enqueue & remove) are only supported
    // if isQueuingSupported() returns true
    public abstract void enqueue(final PlaylistItem item);
    public abstract PlaylistItem remove(String iid);

    // route statistics
    public void updateStatistics() {}
    public String getStatistics() { return ""; }

    // presentation display
    public void updatePresentation() {
        updatePresentation(null);
    }

    public void updatePresentation(Display presentation) {}

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public static Player create(Context context, RouteInfo route) {
        Player player;
        if (route != null && route.supportsControlCategory(
                MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)) {
            return null;
        } else if (route != null) {
            player = new LocalPlayer.SurfaceViewPlayer(context);
        } else {
            player = new LocalPlayer.OverlayPlayer(context);
        }
        player.connect(route);
        return player;
    }

    public interface Callback {
        void onError();
        void onCompletion();
        void onPlaylistChanged();
        void onPlaylistReady();
    }
}
