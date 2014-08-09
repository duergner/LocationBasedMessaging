/*
 * Message.java
 *
 * Copyright (C) 2014 Michael Duergner <michael@duergner.com>
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.duergner.michael.blog.examples.lbm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Michael Duergner <michael@duergner.com>
 */
public class Message extends Base {

    @JsonProperty
    String text;

    @JsonProperty
    String picture;

    @JsonProperty
    Coordinate location;

    @JsonProperty
    String user;

    public Message() {
    }

    public Message(String text, String picture, Coordinate location,
                   String user) {
        this.text = text;
        this.picture = picture;
        this.location = location;
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public String getPicture() {
        return picture;
    }

    public Coordinate getLocation() {
        return location;
    }

    public String getUser() {
        return user;
    }
}
