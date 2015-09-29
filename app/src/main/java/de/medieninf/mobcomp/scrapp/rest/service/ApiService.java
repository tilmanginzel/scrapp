package de.medieninf.mobcomp.scrapp.rest.service;

import java.util.List;

import de.medieninf.mobcomp.scrapp.rest.model.Result;
import de.medieninf.mobcomp.scrapp.rest.model.Rule;
import de.medieninf.mobcomp.scrapp.rest.model.Subscription;
import de.medieninf.mobcomp.scrapp.rest.model.User;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * URL Calls which needs an Identity-Token.
 */
public interface ApiService {

    @POST("/users")
    User createUser(@Body User user);

    @PUT("/users")
    User updateUser(@Body User user);

    @GET("/rules")
    List<Rule> getRules(@Header("Updated-At-Server") String updatedAtServer);

    @GET("/rules/{rule_id}")
    Rule getRuleWithActions(@Path("rule_id") int ruleId,
                            @Header("Updated-At-Server") String updatedAtServer);

    @POST("/rules/{rule_id}/subscriptions")
    Rule createSubscription(@Path("rule_id") int ruleId, @Body Subscription subscription);

    @PUT("/rules/{rule_id}/subscriptions")
    Void updateSubscription(@Path("rule_id") int ruleId, @Body Subscription subscription);

    @DELETE("/rules/{rule_id}/subscriptions")
    Void deleteSubscription(@Path("rule_id") int ruleId);

    @POST("/rules/{rule_id}/result")
    Result createResult(@Path("rule_id") int ruleId, @Body Result result);
}
