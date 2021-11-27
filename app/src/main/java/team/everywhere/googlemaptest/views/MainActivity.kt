package team.everywhere.googlemaptest.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.everywhere.googlemaptest.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.JsonNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import team.everywhere.googlemaptest.R
import team.everywhere.googlemaptest.databinding.ActivityMainBinding
import team.everywhere.googlemaptest.model.PosStationResp
import team.everywhere.googlemaptest.model.StationResp
import team.everywhere.googlemaptest.network.ApiService
import team.everywhere.googlemaptest.network.RetrofitService
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), OnMapReadyCallback, SearchView.OnQueryTextListener,
    SearchResultFragment.OnClickStationListener {
    companion object {
        private const val TAG = "MainActivity"
        const val SERVICE_KEY =
            ""
    }

    var lastSearchedTime = 0

    private lateinit var binding: ActivityMainBinding
    private lateinit var apiService: ApiService
    private lateinit var mMap: GoogleMap
    private val soroutineContext: CoroutineContext get() = Dispatchers.Default
    private val scope = CoroutineScope(soroutineContext)

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var stationArray = ArrayList<StationResp>()
    private var stationPosArray = ArrayList<PosStationResp>()
    private var myLat: Double = 0.0
    private var myLng: Double = 0.0

    private var markerList = ArrayList<MarkerOptions>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        apiService = RetrofitService.provideApi(ApiService::class.java, this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.searchView.setOnQueryTextListener(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.ivRemove.setOnClickListener {
            mMap.clear()
        }
        binding.ivAround.setOnClickListener {
            getNearStations()
        }

        getMyLocation()
    }

    private fun getNearStations() {
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    myLat,
                    myLng
                ), 15.0f
            )
        )
        mMap.clear()
        scope.launch {
            val gson = Gson()
            var result = apiService.getStationByPos(SERVICE_KEY, myLng, myLat, 500, "json")
            if (result.isSuccessful) {
                markerList.clear()
                var body = result.body()
                var msgBody = body?.get("msgBody")!!.asJsonObject
                if (msgBody.get("itemList") is JsonNull) {
                    return@launch
                }
                var itemList =
                    if (msgBody.get("itemList") == null) null else msgBody.get("itemList").asJsonArray
                if (itemList == null) {
                    return@launch
                }
                for (i in 0 until itemList.size()) {
                    var item = gson.fromJson(itemList[i], PosStationResp::class.java)
                    stationPosArray.add(item)

                    val latLng = LatLng(
                        item.gpsY.toDouble(),
                        item.gpsX.toDouble()
                    )
                    var markerOptions = MarkerOptions().apply {
                        position(latLng)
                        title(item.stationNm)
                    }
                    markerList.add(markerOptions)
                    runOnUiThread {
                        mMap.addMarker(markerOptions)
                    }
                }

                if (markerList.size == 0) {
                    Toast.makeText(this@MainActivity, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d(TAG, "onQueryTextSubmit: ${result}")
            }
        }
    }

    private fun getMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            myLat = it.latitude
            myLng = it.longitude
        }.addOnFailureListener { e ->
            e.printStackTrace()
        }

    }

    override fun onMapReady(gMap: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap = gMap
        gMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    37.48398280992932,
                    127.03548479889497
                ), 15.0f
            )
        )
        gMap.isMyLocationEnabled = true
        gMap.setOnMarkerClickListener { mk ->
            gMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        mk.position.latitude,
                        mk.position.longitude
                    ),
                    15.0f
                )
            )
            mk.showInfoWindow()
            true
        }


    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query?.length!! > 0) {
            scope.launch {
                val gson = Gson()
                var result = apiService.getStationByName(SERVICE_KEY, query, "json")
                if (result.isSuccessful) {
                    stationArray.clear()
                    var body = result.body()
                    var msgBody = body?.get("msgBody")!!.asJsonObject
                    var itemList = msgBody.get("itemList").asJsonArray
                    for (i in 0 until itemList.size()) {
                        var item = gson.fromJson(itemList[i], StationResp::class.java)
                        stationArray.add(item)
                    }

                    if (stationArray.size > 0) {
                        var data = Bundle()
                        data.putDouble("myLat", myLat)
                        data.putDouble("myLng", myLng)
                        data.putString("query", query)
                        var fragment = SearchResultFragment(stationArray, this@MainActivity)
                        fragment.arguments = data

                        supportFragmentManager.beginTransaction()
                            .add(R.id.flMain, fragment, "SearchResult")
                            .addToBackStack("SearchResult").commit()
                    } else {
                        Toast.makeText(this@MainActivity, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "onQueryTextSubmit: ${result.errorBody()}")
                }
            }
            return true
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    override fun onClickStation(stationResp: StationResp) {
        val latLng = LatLng(
            stationResp.tmY.toDouble(),
            stationResp.tmX.toDouble()
        )
        var markerOptions = MarkerOptions().apply {
            position(latLng)
            title(stationResp.stNm)
        }

        mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
    }
}