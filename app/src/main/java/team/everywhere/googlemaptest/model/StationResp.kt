package team.everywhere.googlemaptest.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class StationResp(
    @Expose
    @SerializedName("stId")
    var stId: String,
    @Expose
    @SerializedName("stNm")
    var stNm: String,
    @Expose
    @SerializedName("tmX")
    var tmX: String,
    @Expose
    @SerializedName("tmY")
    var tmY: String,
    @Expose
    @SerializedName("posX")
    var posX: String,
    @Expose
    @SerializedName("posY")
    var posY: String,
    @Expose
    @SerializedName("arsId")
    var arsId: String
)
