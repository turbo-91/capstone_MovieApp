package org.example.backend.dtos.netzkino;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public record CustomFields(
        List<String> Adaptives_Streaming,
        List<String> Artikelbild,
        List<String> Duration,
        String productionCountry,
        List<String> featured_img_all,
        List<String> featured_img_all_small,
        List<String> featured_img_seven,
        List<String> featured_img_slider,
        List<String> featured_img_logo,
        List<String> art_logo_img,
        List<String> hero_landscape_img,
        List<String> hero_portrait_img,
        List<String> primary_img,
        List<String> video_still_img,
        OffsetDateTime licenseStart,
        OffsetDateTime licenseEnd,
        List<String> activeCountries,
        String skuAvod,
        String skuSvod,
        boolean drm,
        List<String> FSK,
        List<String> GEO_Availability_Exclusion,
        List<String> IMDb_Bewertung,
        @JsonProperty("IMDb-Link")
        List<String> IMDb_Link,
        List<String> Jahr,
        List<String> offlineAvailable,
        List<String> Regisseur,
        List<String> Stars,
        List<String> Streaming,
        List<String> TV_Movie_Cover,
        List<String> TV_Movie_Genre,
        List<String> Youtube_Deliverry_Active,
        List<String> Youtube_Delivery_Id,
        List<String> Youtube_Delivery_Preview_Only,
        List<String> Youtube_Delivery_Preview_Start,
        List<String> Youtube_Delivery_Preview_End,
        List<String> Featured_Video_Slider,
        List<String> featured_img_seven_small,
        List<String> offlineAvaiable
) {
        /**
         * Helper method to get a value or a default value if the list is null or empty.
         *
         * @param list the list to retrieve the value from
         * @param defaultValue the default value to return if the list is null or empty
         * @return the first value of the list or the default value
         */
        public static String getOrDefault(List<String> list, String defaultValue) {
                return Optional.ofNullable(list)
                        .filter(l -> !l.isEmpty())
                        .map(l -> l.get(0))
                        .orElse(defaultValue);
        }}

