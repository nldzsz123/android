package com;

/**
 * Created by YangLin on 2016-12-02.
 */

public class MavMessageHelp {

    private static final double EARTH_RADIUS = 6378137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    //
//    /**
//     * 根据两点经纬度计算水平距离
//     */
    public static double getDistance(double latitude1, double longitude1,
                                     double latitude2, double longitude2) {
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(Lat1) * Math.cos(Lat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
//    public static double getDistance(double lon1, double lat1, double lon2, double lat2) {
//        double radLat1 = rad(lat1);
//        double radLat2 = rad(lat2);
//        double a = radLat1 - radLat2;
//        double b = rad(lon1) - rad(lon2);
//        double s = Math.acos(Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(rad(lon1)
//                - rad(lon2)) + Math.sin(radLat1) * Math.sin(radLat2)) * EARTH_RADIUS;
////                2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
////                + Math.cos(radLat1) * Math.cos(radLat2)
////                * Math.pow(Math.sin(b / 2), 2)));
////        s = s * EARTH_RADIUS;
//        return s;
//}


    /**
     * 求两经纬度距离
     *
     * @param lon1 第一点的经度
     * @param lat1 第一点的纬度
     * @param lon2 第二点的经度
     * @param lat2 第二点的纬度
     * @return 两点距离，单位m
     */
//    public static double getDistance(double lon1, double lat1,
//                                     double lon2, double lat2) {
//        double radLat1 = rad(lat1);
//        double radLat2 = rad(lat2);
//        double radLon1 = rad(lon1);
//        double radLon2 = rad(lon2);
//        if (radLat1 < 0)
//            radLat1 = Math.PI / 2 + Math.abs(radLat1);// south
//        if (radLat1 > 0)
//            radLat1 = Math.PI / 2 - Math.abs(radLat1);// north
//        if (radLon1 < 0)
//            radLon1 = Math.PI * 2 - Math.abs(radLon1);// west
//        if (radLat2 < 0)
//            radLat2 = Math.PI / 2 + Math.abs(radLat2);// south
//        if (radLat2 > 0)
//            radLat2 = Math.PI / 2 - Math.abs(radLat2);// north
//        if (radLon2 < 0)
//            radLon2 = Math.PI * 2 - Math.abs(radLon2);// west
//        double x1 = Math.cos(radLon1) * Math.sin(radLat1);
//        double y1 = Math.sin(radLon1) * Math.sin(radLat1);
//        double z1 = Math.cos(radLat1);
//
//        double x2 = Math.cos(radLon2) * Math.sin(radLat2);
//        double y2 = Math.sin(radLon2) * Math.sin(radLat2);
//        double z2 = Math.cos(radLat2);
//
//        double d = Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2)
//                + Math.pow((z1 - z2), 2);
//        // // 余弦定理求夹角
//        // double theta = Math.acos((2 - d) / 2);
//
//        d = Math.pow(EARTH_RADIUS, 2) * d;
//        // //余弦定理求夹角
//        double theta = Math.acos((2 * Math.pow(EARTH_RADIUS, 2) - d)
//                / (2 * Math.pow(EARTH_RADIUS, 2)));
//
//        double dist = theta * EARTH_RADIUS;
//        return dist;
//    }


    /**
     * 求两经纬度方向角
     *
     * @param lon1 第一点的经度
     * @param lat1 第一点的纬度
     * @param lon2 第二点的经度
     * @param lat2 第二点的纬度
     * @return 方位角，角度（单位：°）
     */
    public static double getAzimuth(double lon1, double lat1, double lon2,
                                    double lat2) {
        lat1 = rad(lat1);
        lat2 = rad(lat2);
        lon1 = rad(lon1);
        lon2 = rad(lon2);
        double azimuth = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1)
                * Math.cos(lat2) * Math.cos(lon2 - lon1);
        azimuth = Math.sqrt(1 - azimuth * azimuth);
        azimuth = Math.cos(lat2) * Math.sin(lon2 - lon1) / azimuth;
        azimuth = Math.asin(azimuth) * 180 / Math.PI;
        if (Double.isNaN(azimuth)) {
            if (lon1 < lon2) {
                azimuth = 90.0;
            } else {
                azimuth = 270.0;
            }
        }
        return 360 - azimuth;
    }


}
