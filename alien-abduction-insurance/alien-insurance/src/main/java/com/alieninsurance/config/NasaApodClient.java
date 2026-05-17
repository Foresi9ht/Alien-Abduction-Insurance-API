package com.alieninsurance.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
    name = "nasa-client",
    url = "${feign.nasa.url}",
    fallback = NasaApodClientFallback.class
)
public interface NasaApodClient {

    @GetMapping("/planetary/apod")
    Map<String, Object> getAstronomyPictureOfDay(
        @RequestParam("api_key") String apiKey,
        @RequestParam("date") String date
    );
}
