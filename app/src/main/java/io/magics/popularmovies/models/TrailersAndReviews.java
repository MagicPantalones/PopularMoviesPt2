
package io.magics.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrailersAndReviews implements Parcelable
{

    @SerializedName("videos")
    @Expose
    private Trailers trailers;
    @SerializedName("reviews")
    @Expose
    private Reviews reviews;
    public final static Parcelable.Creator<TrailersAndReviews> CREATOR = new Creator<TrailersAndReviews>() {


        @SuppressWarnings({
            "unchecked"
        })
        public TrailersAndReviews createFromParcel(Parcel in) {
            return new TrailersAndReviews(in);
        }

        public TrailersAndReviews[] newArray(int size) {
            return (new TrailersAndReviews[size]);
        }

    }
    ;

    protected TrailersAndReviews(Parcel in) {
        this.trailers = ((Trailers) in.readValue((Trailers.class.getClassLoader())));
        this.reviews = ((Reviews) in.readValue((Reviews.class.getClassLoader())));
    }

    public TrailersAndReviews() {
    }

    public Trailers getTrailers() {
        return trailers;
    }

    public void setTrailers(Trailers trailers) {
        this.trailers = trailers;
    }

    public Reviews getReviews() {
        return reviews;
    }

    public void setReviews(Reviews reviews) {
        this.reviews = reviews;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(trailers);
        dest.writeValue(reviews);
    }

    public int describeContents() {
        return  0;
    }

}
