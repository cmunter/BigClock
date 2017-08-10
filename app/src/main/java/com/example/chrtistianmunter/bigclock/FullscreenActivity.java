package com.example.chrtistianmunter.bigclock;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import org.threeten.bp.LocalTime;

import github.vatsal.easyweather.Helper.TempUnitConverter;
import github.vatsal.easyweather.Helper.WeatherCallback;
import github.vatsal.easyweather.WeatherMap;
import github.vatsal.easyweather.retrofit.models.Weather;
import github.vatsal.easyweather.retrofit.models.WeatherResponseModel;

import static com.example.chrtistianmunter.bigclock.BuildConfig.OWM_API_KEY;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    private static final String TAG = "munter";

    private View mContentView;
    private TickerView timeView;
    private View tempUnitView;
    private RelativeLayout mainContentLayout;
    private LinearLayout weatherContentLayout;
    private ImageView weatherIconView;
    private TextView weatherTextView;
    private TextView weatherDescriptionView;
    private LinearLayout dotLayout;

    private BroadcastReceiver timeTickerReceiver;

    private enum TimeUnit {
        HOUR_MAJOR, HOUR_MINOR, MINUTES_MAJOR, MINUTES_MINOR
    }
    private TimeUnit timeUnit = TimeUnit.HOUR_MAJOR;

    private boolean isTimeShown = true;

    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Removal of status and navigation bar
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "::onCreate");

        setContentView(R.layout.activity_fullscreen);
        hide();

        mContentView = findViewById(R.id.fullscreen_content);
        timeView = findViewById(R.id.time_view);
        tempUnitView = findViewById(R.id.temp_unit_view);
        mainContentLayout = findViewById(R.id.main_content_container);
        weatherContentLayout = findViewById(R.id.weather_content_container);
        weatherIconView = findViewById(R.id.weather_icon);
        weatherTextView = findViewById(R.id.weather_text);
        weatherDescriptionView = findViewById(R.id.weather_description);
        initDotContainer();

        final Button hourMajorEnableButton = findViewById(R.id.hour_major_enable_button);
        final Button hourMinorEnableButton = findViewById(R.id.hour_minor_enable_button);
        final Button minuteMajorEnableButton = findViewById(R.id.minutes_major_enable_button);
        final Button minuteMinorEnableButton = findViewById(R.id.minutes_minor_enable_button);

        hourMajorEnableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeUnit = TimeUnit.HOUR_MAJOR;

                hourMajorEnableButton.setAlpha(1);
                hourMinorEnableButton.setAlpha(0.5f);
                minuteMajorEnableButton.setAlpha(0.5f);
                minuteMinorEnableButton.setAlpha(0.5f);

                setTimeView();
                initDotContainer();
            }
        });

        hourMinorEnableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeUnit = TimeUnit.HOUR_MINOR;

                hourMajorEnableButton.setAlpha(0.5f);
                hourMinorEnableButton.setAlpha(1f);
                minuteMajorEnableButton.setAlpha(0.5f);
                minuteMinorEnableButton.setAlpha(0.5f);

                setTimeView();
                initDotContainer();
            }
        });

        minuteMajorEnableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeUnit = TimeUnit.MINUTES_MAJOR;

                hourMajorEnableButton.setAlpha(0.5f);
                hourMinorEnableButton.setAlpha(0.5f);
                minuteMajorEnableButton.setAlpha(1f);
                minuteMinorEnableButton.setAlpha(0.5f);

                setTimeView();
                initDotContainer();
            }
        });

        minuteMinorEnableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeUnit = TimeUnit.MINUTES_MINOR;

                hourMajorEnableButton.setAlpha(0.5f);
                hourMinorEnableButton.setAlpha(0.5f);
                minuteMajorEnableButton.setAlpha(0.5f);
                minuteMinorEnableButton.setAlpha(1f);

                setTimeView();
                initDotContainer();
            }
        });

        timeView.setCharacterList(TickerUtils.getDefaultNumberList());

        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "::onClick unitTest");

                if(isTimeShown) {
                    getWeather();
                    animateToTemp();
                } else {
                    animateToTime();
                    setTimeView();
                }

                isTimeShown = !isTimeShown;
            }
        });

        setTimeView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initTimeTickerReceiver();
    }

    private void initDotContainer() {
        if(dotLayout!=null) {
            dotLayout.clearAnimation();
            dotLayout.setVisibility(View.GONE);
        }

        if(timeUnit == TimeUnit.HOUR_MINOR) {
            dotLayout = findViewById(R.id.dot_hour_container);
            startDotAnimation();
        } else if (timeUnit == TimeUnit.MINUTES_MAJOR){
            dotLayout = findViewById(R.id.dot_minutes_container);
            startDotAnimation();
        }
    }

    private void startDotAnimation() {
        dotLayout.setVisibility(View.VISIBLE);
        dotLayout.setAlpha(1);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofPropertyValuesHolder(dotLayout,
                PropertyValuesHolder.ofFloat("alpha", 0f));
        alphaAnimator.setDuration(2000);

        alphaAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        alphaAnimator.setRepeatMode(ObjectAnimator.REVERSE);

        alphaAnimator.start();
    }

    private void animateToTemp() {

        mainContentLayout.animate().translationY(400f).scaleX(0.2f).scaleY(0.2f).setInterpolator(new FastOutSlowInInterpolator()).setDuration(500).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if(dotLayout!=null) dotLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                tempUnitView.setVisibility(View.VISIBLE);
                weatherContentLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();

    }

    private void animateToTime() {
        mainContentLayout.animate().translationY(0f).scaleX(1f).scaleY(1f).setInterpolator(new FastOutSlowInInterpolator()).setDuration(500).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                tempUnitView.setVisibility(View.GONE);
                weatherContentLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(dotLayout!=null) dotLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();

    }

    private void getWeather() {
        WeatherMap weatherMap = new WeatherMap(this, OWM_API_KEY);

        String city = "Copenhagen, DK";
        weatherMap.getCityWeather(city, new WeatherCallback() {
            @Override
            public void success(WeatherResponseModel response) {
                Weather weather[] = response.getWeather();
                String weatherMain = weather[0].getMain();

                Double temperature = TempUnitConverter.convertToCelsius(response.getMain().getTemp());
                String wind = response.getWind().getSpeed() + "m/s";

                Log.e(TAG, "::WeatherCallback success " + weatherMain + ", Temp: " + temperature.intValue() + "°C, " + wind);

                timeView.setText(String.valueOf(temperature.intValue()));
                weatherTextView.setText(weatherMain);
                weatherDescriptionView.setText(weather[0].getDescription());

                int weatherIconResource = WeatherIconUtil.getWeatherIconResource(weather[0].getId());
                weatherIconView.setImageResource(weatherIconResource);
            }

            @Override
            public void failure(String s) {
                Log.e(TAG, "::WeatherCallback failure ");
            }

        });
    }

    private void initTimeTickerReceiver() {
        timeTickerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().contentEquals(Intent.ACTION_TIME_TICK) && isTimeShown) {
                    Log.i(TAG, "::onReceive ");
                    setTimeView();
                } else {
                    animateToTime();
                    setTimeView();
                    isTimeShown = true;
                }
            }
        };
        registerReceiver(timeTickerReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(timeTickerReceiver!=null) {
            try {
                unregisterReceiver(timeTickerReceiver);
            } catch (IllegalArgumentException e) {
                // Ignore receiver not registered
            }
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.post(mHidePart2Runnable);
    }

    private void setTimeView() {
        if(timeUnit==TimeUnit.HOUR_MAJOR) {
            int timeValue = LocalTime.now().getHour();

            if(timeValue<10) {
                timeView.setText("0", true);
            }
            else {
                String hourMajorValue =  String.valueOf(timeValue).substring(0,1);
                timeView.setText(hourMajorValue, true);
            }
        } else if(timeUnit==TimeUnit.HOUR_MINOR) {
            int timeValue = LocalTime.now().getHour();

            if(timeValue<10) {
                String hourMinorValue =  String.valueOf(timeValue);
                timeView.setText(hourMinorValue, true);
            }
            else {
                String minuteMinorValue =  String.valueOf(timeValue).substring(1,2);
                timeView.setText(minuteMinorValue, true);
            }
        } else if(timeUnit==TimeUnit.MINUTES_MAJOR) {
            int timeValue = LocalTime.now().getMinute();
            if(timeValue<10) {
                timeView.setText("0", true);
            } else{
                String minuteMinorValue =  String.valueOf(timeValue).substring(0,1);
                timeView.setText(minuteMinorValue, true);
            }
        }else if(timeUnit==TimeUnit.MINUTES_MINOR) {
            int timeValue = LocalTime.now().getMinute();
            if(timeValue<10) {
                String minuteMinorValue =  String.valueOf(timeValue);
                timeView.setText(minuteMinorValue, true);
            } else{
                String minuteMinorValue =  String.valueOf(timeValue).substring(1,2);
                timeView.setText(minuteMinorValue, true);
            }

        }
    }

    private int testTime = 0;

    private void testSetTimeView() {
        String minuteMinorValue =  String.valueOf(testTime);
        timeView.setText(minuteMinorValue, true);
        testTime++;
    }

}
