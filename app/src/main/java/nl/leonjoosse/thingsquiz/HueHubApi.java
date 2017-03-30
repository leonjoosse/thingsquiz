package nl.leonjoosse.thingsquiz;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by Leon on 29-3-2017.
 */

public interface HueHubApi {

    @PUT("/api/{userId}/lights/{bulb}/state")
    Call<Object> updateLightState(@Path("userId") String userId,
                                  @Path("bulb") String bulb,
                                  @Body Map<String, Object> body);
}
