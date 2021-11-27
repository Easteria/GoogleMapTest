package team.everywhere.googlemaptest.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PosStationResp(
    @Expose
    @SerializedName("stationId")
    var stationId: String,
    @Expose
    @SerializedName("stationNm")
    var stationNm: String,
    @Expose
    @SerializedName("gpsX")
    var gpsX: String,
    @Expose
    @SerializedName("gpsY")
    var gpsY: String,
    @Expose
    @SerializedName("arsId")
    var arsId: String,
    @Expose
    @SerializedName("dist")
    var dist: String,
    @Expose
    @SerializedName("posX")
    var posX: String,
    @Expose
    @SerializedName("posY")
    var posY: String,
    @Expose
    @SerializedName("stationTp")
    var stationTp: String
)
