package team.everywhere.googlemaptest.adapter

import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import team.everywhere.googlemaptest.R
import team.everywhere.googlemaptest.model.StationResp
import java.text.DecimalFormat

class StationListAdapter(
    var context: Context,
    var listData: ArrayList<StationResp>,
    var myLatLng: LatLng,
    var onStationItemClickListener: OnStationItemClickListener
) : RecyclerView.Adapter<StationListAdapter.ViewHolder>() {
    interface OnStationItemClickListener {
        fun onItemClicked(stationResp: StationResp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        var view = layoutInflater.inflate(R.layout.item_stations, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listData[position]
        holder.tvStationName.text = item.stNm
        var lat = listData[position].tmY.toDouble()
        var lng = listData[position].tmX.toDouble()

        var dist = FloatArray(1)
        Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, lat, lng, dist)
        var df = DecimalFormat("#.##")
        holder.tvDistance.text = df.format(dist[0] / 1000.0f) + "km"

        holder.itemView.setOnClickListener {
            onStationItemClickListener.onItemClicked(item)
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        var tvStationName: TextView = itemView.findViewById(R.id.tvStationName)
    }
}