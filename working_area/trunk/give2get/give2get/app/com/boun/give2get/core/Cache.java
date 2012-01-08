package com.boun.give2get.core;

import models.Sehir;
import models.Ilce;
import models.Semt;

import java.util.Map;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 19, 2011
 * Time: 9:08:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class Cache {

    private static List<Sehir> sehirler;

    

    public static List<Sehir> getSehirler() {
        return sehirler;
    }

    public static void setSehirler(List<Sehir> sehirler) {
        Cache.sehirler = sehirler;
    }
}
