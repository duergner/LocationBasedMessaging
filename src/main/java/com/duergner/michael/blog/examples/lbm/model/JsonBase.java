/*
 * JsonBase.java
 *
 * Copyright (C) 2014 Michael Duergner <michael@duergner.com>
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.duergner.michael.blog.examples.lbm.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Michael Duergner <michael@duergner.com>
 */
@JsonSerialize(
        include = JsonSerialize.Inclusion.NON_NULL)
public abstract class JsonBase {
}
