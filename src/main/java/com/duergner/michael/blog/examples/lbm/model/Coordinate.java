/*
 * Coordinate.java
 *
 * Copyright (C) 2014 Michael Duergner <michael@duergner.com>
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.duergner.michael.blog.examples.lbm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.geo.GeoHash;

/**
 * @author Michael Duergner <michael@duergner.com>
 */
public class Coordinate extends JsonBase {

    @JsonProperty
    Double latitude;

    @JsonProperty
    Double longitude;

    public Coordinate() {
    }

    public Coordinate(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    String geoHash() {
        return geoHash(6);
    }

    String geoHash(int length) {
        return GeoHash.encodeHash(getLatitude(), getLongitude(), length);
    }
}
