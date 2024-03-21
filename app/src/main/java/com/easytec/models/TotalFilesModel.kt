package com.easytec.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TotalFilesModel(
    var uri: Uri? = null,
    var path: String = "",
    var fileName: String = "",
    var fileSize: Long = 0,
    var dateTime: Long = 0,
    var type: String = "",
) : Parcelable {


    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeString(path)
        parcel.writeString(fileName)
        parcel.writeLong(fileSize)
        parcel.writeLong(dateTime)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TotalFilesModel> {
        override fun createFromParcel(parcel: Parcel): TotalFilesModel {
            return TotalFilesModel(parcel)
        }

        override fun newArray(size: Int): Array<TotalFilesModel?> {
            return arrayOfNulls(size)
        }

    }
}
