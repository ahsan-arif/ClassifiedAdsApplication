package com.android.classifiedapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Report implements Parcelable {
    String id;
    String reason;
    String reportedOn;
    String reportedBy;

    public Report() {
    }

    protected Report(Parcel in) {
        id = in.readString();
        reason = in.readString();
        reportedOn = in.readString();
        reportedBy = in.readString();
    }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReportedOn() {
        return reportedOn;
    }

    public void setReportedOn(String reportedOn) {
        this.reportedOn = reportedOn;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(reason);
        dest.writeString(reportedOn);
        dest.writeString(reportedBy);
    }
}
