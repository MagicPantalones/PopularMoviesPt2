
package io.magics.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrailerResult implements Parcelable
{

    @SerializedName("key")
    @Expose
    private String key;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("site")
    @Expose
    private String site;

    public final static Parcelable.Creator<TrailerResult> CREATOR = new Creator<TrailerResult>() {
        @SuppressWarnings({
            "unchecked"
        })
        public TrailerResult createFromParcel(Parcel in) {
            return new TrailerResult(in);
        }

        public TrailerResult[] newArray(int size) {
            return (new TrailerResult[size]);
        }

    };

    @SuppressWarnings("WeakerAccess")
    protected TrailerResult(Parcel in) {
        this.key = ((String) in.readValue((String.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.site = ((String) in.readValue((String.class.getClassLoader())));
    }

    public TrailerResult() {
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getSite() {
        return site;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(key);
        dest.writeValue(name);
        dest.writeValue(site);
    }

    public int describeContents() {
        return  0;
    }

}
