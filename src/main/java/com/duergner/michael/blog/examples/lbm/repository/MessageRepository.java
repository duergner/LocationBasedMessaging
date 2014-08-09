/*
 * MessageRepository.java
 *
 * Copyright (C) 2014 Michael Duergner <michael@duergner.com>
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.duergner.michael.blog.examples.lbm.repository;

import com.duergner.michael.blog.examples.lbm.model.Message;

import java.util.List;
import java.util.Set;

/**
 * @author Michael Duergner <michael@duergner.com>
 */
public interface MessageRepository extends LBMCrudRepository<Message> {

    List<Message> findAllWithinGeoHashes(Set<String> geoHashes);
}
