package team.everywhere.googlemaptest.network

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import team.everywhere.googlemaptest.R
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object RetrofitService {
    private lateinit var retrofit: Retrofit

    fun provideRetrofit(context: Context): Retrofit {
        val gson = GsonBuilder()
            .setDateFormat("yyyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        retrofit = Retrofit.Builder()
            .baseUrl("http://ws.bus.go.kr/api/rest/stationinfo/")
            .client(provideOkHttpClient(context))
            .addConverterFactory(NullOnEmptyConverterFactory()) //필수는 아님
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofit
    }

    private fun provideOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)   //필수는 아님
            .writeTimeout(60, TimeUnit.SECONDS)     //
            .readTimeout(60, TimeUnit.SECONDS)      //
            .build()
    }

    class NullOnEmptyConverterFactory : Converter.Factory() {
        override fun responseBodyConverter(
            type: Type?,
            annotations: Array<Annotation>?,
            retrofit: Retrofit?
        ): Converter<ResponseBody, *>? {
            val delegate = retrofit!!.nextResponseBodyConverter<Any>(this, type!!, annotations!!)
            return Converter<ResponseBody, Any> {
                if (it.contentLength() == 0L) return@Converter null
                delegate.convert(it)
            }
        }
    }

    fun <T> provideApi(service: Class<T>?, context: Context): T {
        return provideRetrofit(context)!!.create(service)
    }

}