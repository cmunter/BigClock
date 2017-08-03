package com.example.chrtistianmunter.bigclock;

/**
 * Created by chrtistianmunter on 8/3/17.
 */

public class WeatherIconUtil {

    public static int getWeatherIconResource(String idString) {

        int id = Integer.parseInt(idString);

        if(id==500) return R.drawable.ic_weather_rainy;
        else if(id>=501 && id<600) return R.drawable.ic_weather_pouring;
        else if(id>=600 && id<612) return R.drawable.ic_weather_snowy;
        else if(id>=612 && id<700) return R.drawable.ic_weather_snowy_rainy;
        else if(id==741) return R.drawable.ic_weather_fog;
        else if(id==800) return R.drawable.ic_weather_sunny;
        else if(id==801) return R.drawable.ic_weather_partlycloudy;
        else if(id>=801 && id<900) return R.drawable.ic_weather_cloudy;
        else if(id==905) return R.drawable.ic_weather_windy_variant;
        else if(id==906) return R.drawable.ic_weather_hail;
        else if(id>=951 && id<963) return R.drawable.ic_weather_windy_variant;

        return R.drawable.ic_weather_sunset_up;
    }

}
