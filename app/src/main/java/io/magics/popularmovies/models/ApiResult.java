package io.magics.popularmovies.models;

import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import android.os.Parcel;

import com.google.gson.annotations.Expose;

@SuppressWarnings("CanBeFinal")
public class ApiResult implements Parcelable
{

    @SerializedName("page")
    @Expose
    private Integer page;
    @SerializedName("results")
    @Expose
    private List<Movie> movies = null;
    @SerializedName("total_pages")
    @Expose
    private Integer totalPages;

    public static final Parcelable.Creator<ApiResult> CREATOR = new Creator<ApiResult>() {


        @SuppressWarnings({ "unchecked" })
        public ApiResult createFromParcel(Parcel in) {
            return new ApiResult(in);
        }

        public ApiResult[] newArray(int size) {
            return (new ApiResult[size]);
        }

    };

    @SuppressWarnings("WeakerAccess")
    protected ApiResult(Parcel in) {
        this.page = ((Integer) in.readValue((Integer.class.getClassLoader())));
        in.readList(this.movies, (Movie.class.getClassLoader()));
        this.totalPages = ((Integer) in.readValue((Integer.class.getClassLoader())));
    }

    public ApiResult() {}

    public Integer getPage() { return page; }

    public void setPage(Integer page) { this.page = page; }

    public List<Movie> getMovies() { return movies; }

    public void setMovies(List<Movie> movies) { this.movies = movies; }

    public Integer getTotalPages() { return totalPages; }

    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(page);
        dest.writeList(movies);
        dest.writeValue(totalPages);
    }

    public int describeContents() {
        return 0;
    }

}
