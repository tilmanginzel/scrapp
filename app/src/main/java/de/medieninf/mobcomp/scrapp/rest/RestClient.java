package de.medieninf.mobcomp.scrapp.rest;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.medieninf.mobcomp.scrapp.rest.service.ApiService;
import de.medieninf.mobcomp.scrapp.util.Config;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * RestClient encapsulates all REST-Interfaces from Retrofit.
 */
public class RestClient {
    private Context context;

    private ApiService apiService;

    public RestClient(Context context) {
        this.context = context.getApplicationContext();

        // initialize GSON Converter
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES) // match test_key -> testKey
                .create();

        // initialize RestAdapter
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(Config.SERVER_URL)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new IdentityTokenRequestInterceptor(context))
                .setErrorHandler(new RestErrorHandler())
                .build();

        // create Services
        apiService = restAdapter.create(ApiService.class);
    }

    public ApiService getApiService() {
        return apiService;
    }
}
