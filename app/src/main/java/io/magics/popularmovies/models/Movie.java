
package io.magics.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Movie implements Parcelable {

    @SerializedName("poster_path")
    @Expose
    private String posterUrl;
    @SerializedName("overview")
    @Expose
    private String overview;
    @SerializedName("release_date")
    @Expose
    private String releaseDate;
    @SerializedName("id")
    @Expose
    private Integer movieId;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("vote_average")
    @Expose
    private Double voteAverage;

    private int shadowInt = -1;

    public static final Parcelable.Creator<Movie> CREATOR = new Creator<Movie>() {

        @SuppressWarnings({ "unchecked" })
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return (new Movie[size]);
        }
    };

    @SuppressWarnings("WeakerAccess")
    protected Movie(Parcel in) {
        this.posterUrl = ((String) in.readValue((String.class.getClassLoader())));
        this.overview = ((String) in.readValue((String.class.getClassLoader())));
        this.releaseDate = ((String) in.readValue((String.class.getClassLoader())));
        this.movieId = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.title = ((String) in.readValue((String.class.getClassLoader())));
        this.voteAverage = ((Double) in.readValue((Double.class.getClassLoader())));
        this.shadowInt = ((Integer) in.readValue((Integer.class.getClassLoader())));
    }

    public Movie() {}

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public Integer getMovieId() { return movieId; }
    public void setMovieId(Integer movieId) { this.movieId = movieId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }

    public int getShadowInt() { return shadowInt; }
    public void setShadowInt(int shadowInt) { this.shadowInt = shadowInt; }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(posterUrl);
        dest.writeValue(overview);
        dest.writeValue(releaseDate);
        dest.writeValue(movieId);
        dest.writeValue(title);
        dest.writeValue(voteAverage);
        dest.writeValue(shadowInt);
    }

    public int describeContents() {
        return 0;
    }

}
