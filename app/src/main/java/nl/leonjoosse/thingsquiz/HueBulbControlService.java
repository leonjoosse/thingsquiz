package nl.leonjoosse.thingsquiz;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Leon on 29-3-2017.
 */
public class HueBulbControlService extends IntentService {

    public static void teamPressed(Context context, int bulb) {
        context.startService(new Intent(context, HueBulbControlService.class)
                .putExtra(EXTRA_COMMAND, COMMAND_TEAM_PRESSED)
                .putExtra(EXTRA_BULB, bulb));
    }

    public static void answerApproved(Context context, int bulb) {
        context.startService(new Intent(context, HueBulbControlService.class)
                .putExtra(EXTRA_COMMAND, COMMAND_ANSWER_APPROVED)
                .putExtra(EXTRA_BULB, bulb));
    }

    public static void answerDeclined(Context context, int bulb) {
        context.startService(new Intent(context, HueBulbControlService.class)
                .putExtra(EXTRA_COMMAND, COMMAND_ANSWER_DECLINED)
                .putExtra(EXTRA_BULB, bulb));

    }

    public static void turnOff(Context context, int bulb) {
        context.startService(new Intent(context, HueBulbControlService.class)
                .putExtra(EXTRA_COMMAND, COMMAND_TURN_OFF)
                .putExtra(EXTRA_BULB, bulb));
    }

    public static final String TAG = HueBulbControlService.class.getSimpleName();

    public static final String HUE_HUB_IP = "192.168.0.101";
    public static final String HUE_HUB_USERNAME = "2yWmiKGU9M0GXTdqPXTOeW9CGpS-ZBrw98z0c8Ot";

    public static final String EXTRA_BULB = "bulb";
    public static final String EXTRA_COMMAND = "command";

    private static final int BULB_INVALID = 0;
    public static final int BULB_1 = 1;
    public static final int BULB_2 = 2;
    public static final List<Integer> bulbs = Arrays.asList(BULB_1, BULB_2);

    private static final int COMMAND_INVALID = 0;
    public static final int COMMAND_TEAM_PRESSED = 1;
    public static final int COMMAND_ANSWER_APPROVED = 2;
    public static final int COMMAND_ANSWER_DECLINED = 3;
    public static final int COMMAND_TURN_OFF = 4;


    public HueBulbControlService() {
        super(HueBulbControlService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int bulb = intent.getIntExtra(EXTRA_BULB, BULB_INVALID);
        int command = intent.getIntExtra(EXTRA_COMMAND, COMMAND_INVALID);

        if (!bulbs.contains(bulb)) {
            Log.d(TAG, "Unknown bulb '" + bulb + "'");
            return;
        }

        Map<String, Object> body = new HashMap<>();

        switch (command) {
            case COMMAND_TEAM_PRESSED:
                body.put("on", true);
                body.put("hue", 0); // red (could also be 65535
                body.put("transitiontime", 1); // 1 = 100 millis, can't go lower
                body.put("bri", 254); // full on
                body.put("sat", 0);
                break;
            case COMMAND_ANSWER_APPROVED:
                body.put("on", true);
                body.put("hue", 25500); // green
                body.put("transitiontime", 1); // 1 = 100 millis, can't go lower
                body.put("bri", 254); // full on
                body.put("sat", 254);
                break;
            case COMMAND_ANSWER_DECLINED:
                body.put("on", true);
                body.put("hue", 0); // red (could also be 65535
                body.put("transitiontime", 1); // 1 = 100 millis, can't go lower
                body.put("bri", 254); // full on
                body.put("sat", 254);
                break;
            case COMMAND_TURN_OFF:
                body.put("on", false);
                body.put("transitiontime", 1); // 1 = 100 millis, can't go lower
                break;
            default:
                Log.d(TAG, "Unknown command '" + command + "'");
                return;
        }

        try {
            Response<Object> response = new Retrofit.Builder()
                    .baseUrl("http://" + HUE_HUB_IP)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(HueHubApi.class)
                    .updateLightState(HUE_HUB_USERNAME, String.valueOf(bulb), body)
                    .execute();
            Log.d(TAG, "onHandleIntent: response: " + String.valueOf(response.body()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
