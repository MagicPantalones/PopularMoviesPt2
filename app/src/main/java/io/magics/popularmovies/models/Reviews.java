
package io.magics.popularmovies.models;

import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Reviews implements Parcelable
{

    @SerializedName("page")
    @Expose
    private Integer page;
    @SuppressWarnings("CanBeFinal")
    @SerializedName("results")
    @Expose
    private List<ReviewResult> reviewResults = null;
    @SerializedName("total_pages")
    @Expose
    private Integer totalPages;
    @SerializedName("total_results")
    @Expose
    private Integer totalResults;
    public static final Parcelable.Creator<Reviews> CREATOR = new Creator<Reviews>() {


        @SuppressWarnings({
            "unchecked"
        })
        public Reviews createFromParcel(Parcel in) {
            return new Reviews(in);
        }

        public Reviews[] newArray(int size) {
            return (new Reviews[size]);
        }

    }
    ;

    @SuppressWarnings("WeakerAccess")
    protected Reviews(Parcel in) {
        this.page = ((Integer) in.readValue((Integer.class.getClassLoader())));
        in.readList(this.reviewResults, (ReviewResult.class.getClassLoader()));
        this.totalPages = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.totalResults = ((Integer) in.readValue((Integer.class.getClassLoader())));
    }

    public Reviews() {
    }

    public List<ReviewResult> getReviewResults() {
        return reviewResults;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(page);
        dest.writeList(reviewResults);
        dest.writeValue(totalPages);
        dest.writeValue(totalResults);
    }

    public int describeContents() {
        return  0;
    }

}
