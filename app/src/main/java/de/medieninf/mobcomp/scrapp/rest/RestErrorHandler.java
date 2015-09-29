package de.medieninf.mobcomp.scrapp.rest;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Custom error handler for retrofit requests.
 */
class RestErrorHandler implements ErrorHandler {
    @Override
    public Throwable handleError(RetrofitError cause) {
        Response r = cause.getResponse();
        if (r != null && r.getStatus() == 401) {
            return cause;
        }
        return cause;
    }
}
