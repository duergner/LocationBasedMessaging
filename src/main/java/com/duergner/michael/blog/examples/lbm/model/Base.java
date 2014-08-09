/*
 * Base.java
 *
 * Copyright (C) 2014 Michael Duergner <michael@duergner.com>
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.duergner.michael.blog.examples.lbm.model;

import com.basho.riak.client.api.cap.VClock;
import com.basho.riak.client.core.util.BinaryValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * @author Michael Duergner <michael@duergner.com>
 */
public abstract class Base extends JsonBase {

    @JsonIgnore
    public VClock vClock;

    @JsonIgnore
    public BinaryValue binaryValue;

    @JsonProperty
    String id;

    @JsonProperty
    Date dateCreated;

    @JsonProperty
    Date lastUpdated;

    protected Base() {
        Date now = new Date();
        dateCreated = now;
        lastUpdated = now;
    }

    public String getId() {
        return id;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected void markUpdated() {
        lastUpdated = new Date();
    }
}
