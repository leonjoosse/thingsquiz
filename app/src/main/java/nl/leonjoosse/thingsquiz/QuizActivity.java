package nl.leonjoosse.thingsquiz;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Leon on 29-3-2017.
 */

public class QuizActivity extends Activity {

    public static final List<String> questions = Arrays.asList(
            "Which API level is Android Things preview 2 running on?",
            "Name one other city where Q42 has an office.",
            ""
    );

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final long WAITING_TIME_MILLIS = 2000L;

    public static final int TEAM_ONE = 1;
    public static final int TEAM_TWO = 2;

    public static final int KEYCODE_TEAM_ONE = KeyEvent.KEYCODE_CTRL_LEFT;
    public static final int KEYCODE_TEAM_TWO = KeyEvent.KEYCODE_CTRL_RIGHT;
    public static final int KEYCODE_APPROVE = KeyEvent.KEYCODE_Y;
    public static final int KEYCODE_DECLINE = KeyEvent.KEYCODE_N;
    public static final int KEYCODE_RESTART = KeyEvent.KEYCODE_R;

    static final int
            STATE_WAIT_FOR_START = 1,
            STATE_WAIT_FOR_NEXT_QUESTION = 2,
            STATE_SHOW_QUESTION = 3,
            STATE_ON_TEAM_PRESSED = 4,
            STATE_ON_GAME_LEADER_DECISION = 5,
            STATE_ON_END = 6;

    int currentState;
    int teamThatPressed;
    int currentQuestionIndex;
    SparseIntArray answers;

    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        answers = new SparseIntArray(questions.size());

        moveToState(STATE_WAIT_FOR_START);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void moveToState(int newState) {

        Log.i(TAG, "moveToState: newState " + newState);

        switch (newState) {

            // Show simple wait screen, press any key to start quiz
            case STATE_WAIT_FOR_START:
                showStartScreen();
                break;

            case STATE_WAIT_FOR_NEXT_QUESTION:
                showWaitForNextQuestionScreen();
                break;

            case STATE_SHOW_QUESTION:
                showQuestion();
                break;

            case STATE_ON_TEAM_PRESSED:
                showTeamPressed();
                break;

            case STATE_ON_GAME_LEADER_DECISION:
                showGameLeaderDecision();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HueBulbControlService.turnOff(QuizActivity.this, teamThatPressed);
                        goToNextQuestionOrEnd();
                    }
                }, WAITING_TIME_MILLIS);
                break;

            case STATE_ON_END:
                showEndScreen();
                break;

            default:
                Log.d(TAG, "moveToState: unknown state " + newState);
                return;
        }

        currentState = newState;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KEYCODE_RESTART) {
            reset();
        }

        if (currentState == STATE_WAIT_FOR_START) {
            prepareForAndMoveToFirstQuestion();
            moveToState(STATE_WAIT_FOR_NEXT_QUESTION);
        }

        if (currentState == STATE_SHOW_QUESTION) {

            if (keyCode == KEYCODE_TEAM_ONE) {
                teamThatPressed = TEAM_ONE;
                moveToState(STATE_ON_TEAM_PRESSED);
                return true;
            }

            if (keyCode == KEYCODE_TEAM_TWO) {
                teamThatPressed = TEAM_TWO;
                moveToState(STATE_ON_TEAM_PRESSED);
                return true;
            }
        }

        if (currentState == STATE_ON_TEAM_PRESSED) {

            if (keyCode == KEYCODE_APPROVE) {
                answers.put(currentQuestionIndex, teamThatPressed);
                moveToState(STATE_ON_GAME_LEADER_DECISION);
                return true;
            }

            if (keyCode == KEYCODE_DECLINE) {
                answers.put(currentQuestionIndex, 0);
                moveToState(STATE_ON_GAME_LEADER_DECISION);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showStartScreen() {
        Log.d(TAG, "showStartScreen: showing. Press any button to start quiz");
        currentQuestionIndex = -1;
    }

    private void goToNextQuestionOrEnd() {

        boolean questionsLeft = currentQuestionIndex != questions.size() - 1;

        Log.d(TAG, "goToNextQuestion: curIdx " + currentQuestionIndex + ", size: " + questions.size());

        if (questionsLeft) {
            currentQuestionIndex++;
            moveToState(STATE_WAIT_FOR_NEXT_QUESTION);
        } else {
            moveToState(STATE_ON_END);
        }
    }

    private void prepareForAndMoveToFirstQuestion() {
        currentQuestionIndex = 0;

    }

    private void showQuestion() {
        Log.d(TAG, "showQuestion: " + questions.get(currentQuestionIndex));
    }

    private void showTeamPressed() {
        Log.d(TAG, "showTeamPressed: team " + teamThatPressed + " was first (turning on light bulb)");
        Log.d(TAG, "showTeamPressed: team: give your answer");
        Log.d(TAG, "showTeamPressed: game leader: approve or decline answer");

        HueBulbControlService.teamPressed(this, teamThatPressed);
    }

    private void showGameLeaderDecision() {

        int team = answers.get(currentQuestionIndex);

        if (team == 0) {
            Log.d(TAG, "showGameLeaderDecision: no points this round");
            HueBulbControlService.answerDeclined(this, teamThatPressed);
        } else {
            Log.d(TAG, "showGameLeaderDecision: 1 point for team " + team);
            HueBulbControlService.answerApproved(this, teamThatPressed);
        }
    }

    private void showEndScreen() {

        int team1 = 0, team2 = 0;

        for (int i = 0, size = answers.size(); i < size; i++) {
            int scoreForTeam = answers.get(i);

            if (scoreForTeam == TEAM_ONE) {
                team1++;
            }

            if (scoreForTeam == TEAM_TWO) {
                team2++;
            }
        }

        Log.d(TAG, "showEndScreen: team 1 has " + team1 + " points");
        Log.d(TAG, "showEndScreen: team 2 has " + team2 + " points");

        if (team1 == team2) {
            Log.d(TAG, "showEndScreen: it's a draw!");
        } else if (team1 > team2) {
            Log.d(TAG, "showEndScreen: team1 won the quiz!");
        } else {
            Log.d(TAG, "showEndScreen: team2 won the quiz!");
        }
    }

    private void showWaitForNextQuestionScreen() {
        Log.d(TAG, "showWaitForNextQuestionScreen: waiting 5 sec to show next question");
        Log.d(TAG, "showWaitForNextQuestionScreen: currentQuestionIndex: " + currentQuestionIndex);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveToState(STATE_SHOW_QUESTION);
            }
        }, WAITING_TIME_MILLIS);
    }

    private void reset() {

        Log.i(TAG, "reset");

        currentState = 0;
        currentQuestionIndex = 0;
        teamThatPressed = 0;
        answers.clear();
        moveToState(STATE_WAIT_FOR_START);

        HueBulbControlService.turnOff(this, TEAM_ONE);
        HueBulbControlService.turnOff(this, TEAM_TWO);
    }

    /*
    1. load quiz
    2. show question on screen
    3. enable button presses
    4. wait for button press
    5. on button press, store who pressed
    6. disable other button presses
    7. show who pressed
    8. turn on hue bulb for team that pressed
    9. team gives answer
   10. game leader approves with KEYCODE_APPROVE or declines with KEYCODE_DECLINE
   11. on approve, store a point for the team that pressed first
   12. turn off light bulb
   13. if any question left, go to step 2
   14. else, show results
   15. press KEYCODE_RESTART to restart the quiz
     */
}
