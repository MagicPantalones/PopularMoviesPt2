
package io.magics.popularmovies.models;

import java.util.ArrayList;
import java.util.List;
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
    @SerializedName("genre_ids")
    @Expose
    private List<Integer> genreIds = new ArrayList<>();
    @SerializedName("id")
    @Expose
    private Integer movieId;
    @SerializedName("original_title")
    @Expose
    private String originalTitle;
    @SerializedName("original_language")
    @Expose
    private String originalLanguage;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("backdrop_path")
    @Expose
    private String backdropPath;
    @SerializedName("vote_count")
    @Expose
    private Integer voteCount;
    @SerializedName("vote_average")
    @Expose
    private Double voteAverage;

    public static final Parcelable.Creator<Movie> CREATOR = new Creator<Movie>() {

        @SuppressWarnings({ "unchecked" })
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return (new Movie[size]);
        }
    };

    protected Movie(Parcel in) {
        this.posterUrl = ((String) in.readValue((String.class.getClassLoader())));
        this.overview = ((String) in.readValue((String.class.getClassLoader())));
        this.releaseDate = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.genreIds, (java.lang.Integer.class.getClassLoader()));
        this.movieId = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.originalTitle = ((String) in.readValue((String.class.getClassLoader())));
        this.originalLanguage = ((String) in.readValue((String.class.getClassLoader())));
        this.title = ((String) in.readValue((String.class.getClassLoader())));
        this.backdropPath = ((String) in.readValue((String.class.getClassLoader())));
        this.voteCount = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.voteAverage = ((Double) in.readValue((Double.class.getClassLoader())));
    }

    public Movie() {}

    public String getPosterUrl() { return posterUrl; }

    public String getOverview() { return overview; }

    public String getReleaseDate() { return releaseDate; }

    public List<Integer> getGenreIds() { return genreIds; }

    public Integer getMovieId() { return movieId; }

    public String getOriginalTitle() { return originalTitle; }

    public String getOriginalLanguage() { return originalLanguage; }

    public String getTitle() { return title; }

    public String getBackdropPath() { return backdropPath; }

    public Integer getVoteCount() { return voteCount; }

    public Double getVoteAverage() { return voteAverage; }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(posterUrl);
        dest.writeValue(overview);
        dest.writeValue(releaseDate);
        dest.writeList(genreIds);
        dest.writeValue(movieId);
        dest.writeValue(originalTitle);
        dest.writeValue(originalLanguage);
        dest.writeValue(title);
        dest.writeValue(backdropPath);
        dest.writeValue(voteCount);
        dest.writeValue(voteAverage);
    }

    public int describeContents() {
        return 0;
    }



}
