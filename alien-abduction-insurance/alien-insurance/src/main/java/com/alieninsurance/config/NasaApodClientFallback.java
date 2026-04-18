package com.alieninsurance.config;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NasaApodClientFallback implements NasaApodClient {

    @Override
    public Map<String, Object> getAstronomyPictureOfDay(String apiKey, String date) {
        return Map.of(
            "title", "Galactic data unavailable",
            "explanation", "NASA API could not be reached at this time",
            "date", date,
            "media_type", "fallback"
        );
    }
}
