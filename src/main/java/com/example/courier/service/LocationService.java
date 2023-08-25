package com.example.courier.service;

import org.springframework.stereotype.Service;

@Service
public class LocationService {

    //полюс
    static double latitudePol = 44;
    static double longitudePol = 76.89460011279164;

    //fe
    static double latitudeFe = 43.24402156690334;
    static double longitudeFe = 76.89066499039188;

    public Double angleBetweenVerticalAndPoint(Double lat1, Double lon1){

        double a = disstanceBetweenPoints(latitudePol, longitudePol, latitudeFe, longitudeFe);
        System.out.println("a = "+a);

        double b = disstanceBetweenPoints(latitudeFe, longitudeFe, lat1, lon1);
        System.out.println("b = "+b);

        double c = disstanceBetweenPoints(lat1, lon1, latitudePol, longitudePol);
        System.out.println("c = "+c);

        // Найдем угол между сторонами A и B (в радианах)
        double cosC = (a * a + b * b - c * c) / (2 * a * b);
        double angleC = Math.acos(cosC);

        // Переведем угол из радианов в градусы
        double angleDegrees = Math.toDegrees(angleC);

        System.out.println("Угол между сторонами A и B: " + angleDegrees + " градусов");
        if(lon1<longitudeFe){
            angleDegrees = angleDegrees+180;
        }
        return angleDegrees;
    }

    public Double disstanceBetweenPoints(Double lat1, Double lon1,Double lat2, Double lon2){

        double earthRadiusKm = 6371;

        // Разница между широтами и долготами точек
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Вычисление синусов и косинусов половинных углов между точками
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        // Вычисление центрального угла между точками
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;

    }


}
