
package io.magics.popularmovies.models;

import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Trailers implements Parcelable
{

    @SerializedName("results")
    @Expose
    private List<TrailerResult> trailerResults = null;
    public final static Parcelable.Creator<Trailers> CREATOR = new Creator<Trailers>() {


        @SuppressWarnings({
            "unchecked"
        })
        public Trailers createFromParcel(Parcel in) {
            return new Trailers(in);
        }

        public Trailers[] newArray(int size) {
            return (new Trailers[size]);
        }

    }
    ;

    protected Trailers(Parcel in) {
        in.readList(this.trailerResults, (TrailerResult.class.getClassLoader()));
    }

    public Trailers() {
    }

    public List<TrailerResult> getTrailerResults() {
        return trailerResults;
    }

    public void setTrailerResults(List<TrailerResult> trailerResults) {
        this.trailerResults = trailerResults;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(trailerResults);
    }

    public int describeContents() {
        return  0;
    }

}
