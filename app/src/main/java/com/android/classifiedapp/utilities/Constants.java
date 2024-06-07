package com.android.classifiedapp.utilities;

public class Constants {
    public static String NOTIFICATION_URL = "https://fcm.googleapis.com/v1/projects/ecommerceapp-65596/messages:send";
    public static String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    public static String[] SCOPES = { MESSAGING_SCOPE };
    private static final double EARTH_RADIUS = 6371000; // Earth's radius in meters
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate the differences between coordinates
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Calculate the Haversine formula
        double a = Math.pow(Math.sin(deltaLat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(deltaLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        return distance;
    }
}
