package com.liskovsoft.googleapi.common.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.liskovsoft.googleapi.common.converters.gson.GsonConverterFactory;
import com.liskovsoft.googleapi.common.converters.jsonpath.converter.JsonPathConverterFactory;
import com.liskovsoft.googleapi.common.converters.jsonpath.converter.JsonPathSkipConverterFactory;
import com.liskovsoft.googleapi.common.converters.jsonpath.typeadapter.JsonPathSkipTypeAdapter;
import com.liskovsoft.googleapi.common.converters.jsonpath.typeadapter.JsonPathTypeAdapter;
import com.liskovsoft.googleapi.common.converters.querystring.converter.QueryStringConverterFactory;
import com.liskovsoft.googleapi.common.converters.regexp.converter.RegExpConverterFactory;
import com.liskovsoft.googleapi.common.models.gen.ErrorResponse;

import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.List;

public class RetrofitHelper {
    // Ignored when specified url is absolute
    private static final String DEFAULT_BASE_URL = "https://www.youtube.com";

    public static <T> T withGson(Class<T> clazz) {
        return buildRetrofit(GsonConverterFactory.create()).create(clazz);
    }

    public static <T> T withJsonPath(Class<T> clazz) {
        return buildRetrofit(JsonPathConverterFactory.create()).create(clazz);
    }

    /**
     * Skips first line of the response
     */
    public static <T> T withJsonPathSkip(Class<T> clazz) {
        return buildRetrofit(JsonPathSkipConverterFactory.create()).create(clazz);
    }

    public static <T> T withQueryString(Class<T> clazz) {
        return buildRetrofit(QueryStringConverterFactory.create()).create(clazz);
    }

    public static <T> T withRegExp(Class<T> clazz) {
        return buildRetrofit(RegExpConverterFactory.create()).create(clazz);
    }

    public static <T> T get(Call<T> wrapper) {
        return get(wrapper, false);
    }

    public static <T> T getWithErrors(Call<T> wrapper) {
        return get(wrapper, true);
    }

    public static <T> T get(Call<T> wrapper, boolean withErrors) {
        Response<T> response = getResponse(wrapper);

        if (withErrors) {
            handleResponseErrors(response);
        }

        return response != null ? response.body() : null;
    }

    public static <T> Headers getHeaders(Call<T> wrapper) {
        Response<T> response = getResponse(wrapper);

        return response != null ? response.headers() : null;
    }

    public static <T> Response<T> getResponse(Call<T> wrapper) {
        try {
            return wrapper.execute();
        } catch (ConnectException e) {
            // ConnectException - server is down or address is banned
            // Usually happen on sites like returnyoutubedislikeapi.com
            // We could skip it safe?
            e.printStackTrace();
        } catch (SocketException e) {
            // SocketException - no internet
            // ConnectException - server is down or address is banned
            //wrapper.cancel(); // fix background running when RxJava object is disposed?
            e.printStackTrace();
            throw new IllegalStateException(e); // notify caller about network condition
        } catch (IOException e) {
            // InterruptedIOException - Thread interrupted. Thread died!!
            // UnknownHostException: Unable to resolve host (DNS error) Thread died?
            // Don't rethrow!!! These exceptions cannot be caught inside RxJava!!! Thread died!!!
            e.printStackTrace();
        }

        return null;
    }

    public static <T> JsonPathTypeAdapter<T> adaptJsonPathSkip(Class<?> clazz) {
        Configuration conf = Configuration
                .builder()
                .mappingProvider(new GsonMappingProvider())
                .jsonProvider(new GsonJsonProvider())
                .build();

        ParseContext parser = JsonPath.using(conf);

        return new JsonPathSkipTypeAdapter<>(parser, clazz);
    }

    public static Retrofit buildRetrofit(Converter.Factory factory) {
        Retrofit.Builder builder = createBuilder();

        return builder
                .addConverterFactory(factory)
                .build();
    }

    private static Retrofit.Builder createBuilder() {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder().baseUrl(DEFAULT_BASE_URL);

        retrofitBuilder.client(RetrofitOkHttpHelper.getClient());

        return retrofitBuilder;
    }

    /**
     * Get cookie pair: cookieName=cookieValue
     */
    public static <T> String getCookie(Response<T> response, String cookieName) {
        if (response == null) {
            return null;
        }

        List<String> cookies = response.headers().values("Set-Cookie");

        for (String cookie : cookies) {
            if (cookie.startsWith(cookieName)) {
                return cookie.split(";")[0];
            }
        }

        return null;
    }

    private static <T> void handleErrors(Response<T> response) {
        if (response == null || response.body() != null) {
            return;
        }

        if (response.code() == 400 || response.code() == 409) { // not exists or already exists
            try (ResponseBody body = response.errorBody()) {
                Gson gson = new GsonBuilder().create();
                ErrorResponse error = body != null ? gson.fromJson(body.string(), ErrorResponse.class) : null;
                String message = error != null && error.getError() != null ? error.getError().getMessage() : String.format("Unknown %s error", response.code());
                throw new IllegalStateException(message);
            } catch (IOException e) {
                // handle failure to read error
            }
        }
    }

    private static <T> void handleResponseErrors(Response<T> response) {
        if (response == null || response.body() != null) {
            return;
        }

        if (response.code() == 400 || response.code() == 403) {
            Gson gson = new GsonBuilder().create();
            try (ResponseBody body = response.errorBody()) {
                String errorData = body != null ? body.string() : null;

                ErrorResponse error = errorData != null ? gson.fromJson(errorData, ErrorResponse.class) : null;
                String errorMsg = error != null && error.getError() != null ? ErrorResponse.class.getSimpleName() + ": " + error.getError().getMessage() : null;

                errorMsg = errorMsg != null ? errorMsg : String.format("Unknown %s error", response.code());

                throw new IllegalStateException(errorMsg);
            } catch (IOException e) {
                // handle failure to read error
            }
        }
    }
}
