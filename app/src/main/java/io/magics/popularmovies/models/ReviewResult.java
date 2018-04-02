
package io.magics.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReviewResult implements Parcelable
{

    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("content")
    @Expose
    private String content;

    public final static Parcelable.Creator<ReviewResult> CREATOR = new Creator<ReviewResult>() {


        @SuppressWarnings({
            "unchecked"
        })
        public ReviewResult createFromParcel(Parcel in) {
            return new ReviewResult(in);
        }

        public ReviewResult[] newArray(int size) {
            return (new ReviewResult[size]);
        }

    }
    ;

    protected ReviewResult(Parcel in) {
        this.author = ((String) in.readValue((String.class.getClassLoader())));
        this.content = ((String) in.readValue((String.class.getClassLoader())));
    }

    public ReviewResult() {
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(author);
        dest.writeValue(content);
    }

    public int describeContents() {
        return  0;
    }

}
