package team.everywhere.googlemaptest.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.databinding.adapters.SearchViewBindingAdapter
import androidx.fragment.app.Fragment
import com.everywhere.googlemaptest.databinding.FragmentSearchResultBinding
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import team.everywhere.googlemaptest.R
import team.everywhere.googlemaptest.adapter.StationListAdapter
import team.everywhere.googlemaptest.databinding.FragmentSearchResultBinding
import team.everywhere.googlemaptest.model.StationResp
import team.everywhere.googlemaptest.network.ApiService
import team.everywhere.googlemaptest.network.RetrofitService
import kotlin.coroutines.CoroutineContext

class SearchResultFragment(
    stationsArray: ArrayList<StationResp>,
    var onClickStationListener: OnClickStationListener
) : Fragment(), StationListAdapter.OnStationItemClickListener {
    companion object {
        private const val TAG = "SearchResultFragment"
    }

    interface OnClickStationListener {
        fun onClickStation(stationResp: StationResp)
    }

    lateinit var binding: FragmentSearchResultBinding
    private lateinit var apiService: ApiService
    private val soroutineContext: CoroutineContext get() = Dispatchers.Default
    private val scope = CoroutineScope(soroutineContext)

    private var myLat = 0.0
    private var myLng = 0.0
    private var searchedQuery = ""
    private var stationsArray = stationsArray

    lateinit var slAdapter: StationListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (null != requireActivity().currentFocus) imm.hideSoftInputFromWindow(
            requireActivity().currentFocus!!
                .applicationWindowToken, 0
        )
        binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        apiService = RetrofitService.provideApi(ApiService::class.java, requireContext())


        val view = binding.root
        if (arguments != null) {
            myLat = requireArguments().getDouble("myLat")
            myLng = requireArguments().getDouble("myLng")
            requireArguments().getString("query")?.let {
                searchedQuery = it
                binding.searchView.setQuery(searchedQuery, false)
            }
        }

        initView()

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        slAdapter = StationListAdapter(requireContext(), stationsArray, LatLng(myLat, myLng), this)
        binding.rvResults.adapter = slAdapter
        slAdapter.notifyDataSetChanged()

        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query?.length!! > 0) {
                    scope.launch {
                        val gson = Gson()
                        var result =
                            apiService.getStationByName(MainActivity.SERVICE_KEY, query, "json")
                        if (result.isSuccessful) {
                            var body = result.body()
                            var msgBody = body?.get("msgBody")!!.asJsonObject
                            var itemList = msgBody.get("itemList").asJsonArray
                            stationsArray.clear()
                            for (i in 0 until itemList.size()) {
                                var item = gson.fromJson(itemList[i], StationResp::class.java)
                                stationsArray.add(item)
                            }
                            if (stationsArray.size == 0) {
                                Toast.makeText(requireContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            // 그냥 에러나는거 보여주기 runOnUiThread를 위한
                            requireActivity().runOnUiThread {
                                slAdapter.notifyDataSetChanged()
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
        })

    }

    override fun onItemClicked(stationResp: StationResp) {
        onClickStationListener.onClickStation(stationResp)
        requireActivity().supportFragmentManager.popBackStackImmediate()
    }
}