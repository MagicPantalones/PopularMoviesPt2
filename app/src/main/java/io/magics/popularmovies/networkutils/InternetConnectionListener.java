package io.magics.popularmovies.networkutils;

public interface InternetConnectionListener {
    void onInternetUnavailable();
    void onInternetAvailable();
}
