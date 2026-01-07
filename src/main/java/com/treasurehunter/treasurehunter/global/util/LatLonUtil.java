package com.treasurehunter.treasurehunter.global.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LatLonUtil {

    private static final double EARTH_RADIUS = 6371.0; // km

    /**
     * 두 위도 경도 지점에 대해 km 거리를 계산하는 메서드
     * null 값 들어올 시 -1.0을 리턴함
     * @param fromLat 첫번째 위도
     * @param fromLon 첫번째 경도
     * @param toLat 두번째 위도
     * @param toLon 두번째 경도
     * @return km 거리
     */
    public double latLonDistance(
            final BigDecimal fromLat,
            final BigDecimal fromLon,
            final BigDecimal toLat,
            final BigDecimal toLon
    ) {

        // NPE 방지
        if (fromLat == null || fromLon == null || toLat == null || toLon == null) {
            return -1.0;
        }

        final double deltaLat = Math.toRadians(toLat.subtract(fromLat).doubleValue());
        final double deltaLon = Math.toRadians(toLon.subtract(fromLon).doubleValue());

        //하버사인 공식 이용
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(Math.toRadians(fromLat.doubleValue())) * Math.cos(Math.toRadians(toLat.doubleValue())) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
