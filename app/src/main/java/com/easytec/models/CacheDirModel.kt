package com.easytec.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson

data class CacheDirModel(
    var path: String = "",
    var fileName: String = "",
    var fileSize: Long = 0,
    var dateTime: Long = 0,
    var type: String = ""
): Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()?:"", parcel.readString()?:"", parcel.readLong(), parcel.readLong(), parcel.readString()?:"") {
    }
    fun toJson(): String = Gson().toJson(this)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeString(fileName)
        parcel.writeLong(fileSize)
        parcel.writeLong(dateTime)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CacheDirModel> {
        override fun createFromParcel(parcel: Parcel): CacheDirModel {
            return CacheDirModel(parcel)
        }

        override fun newArray(size: Int): Array<CacheDirModel?> {
            return arrayOfNulls(size)
        }

       fun fromJson(json: String): CacheDirModel {
           return Gson().fromJson(json, CacheDirModel::class.java)
       }

    }

}

