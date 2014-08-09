/*
 * MessageRiakRepository.java
 *
 * Copyright (C) 2014 Michael Duergner <michael@duergner.com>
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.duergner.michael.blog.examples.lbm.repository.riak;

import com.basho.riak.client.api.commands.FetchSet;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.util.BinaryValue;
import com.duergner.michael.blog.examples.lbm.model.Message;
import com.duergner.michael.blog.examples.lbm.repository.MessageRepository;
import com.github.davidmoten.geo.GeoHash;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author Michael Duergner <michael@duergner.com>
 */
public class MessageRiakRepository
        extends AbstractRiakLBMCrudRepository<Message>
        implements MessageRepository {

    static final BinaryValue INDEX_LOCATION =
            BinaryValue.create("message.location", CHARSET);

    static final BinaryValue INDEX_USER =
            BinaryValue.create("message.user", CHARSET);


    public MessageRiakRepository() {
        super(Message.class);
    }

    @Override
    public List<Message> findAllWithinGeoHashes(Set<String> geoHashes) {
        List<Message> result = new ArrayList<>();
        for (String geoHash : geoHashes) {
            try {
                Location location = new Location(new Namespace(
                        AbstractRiakLBMCrudRepository.SET_BUCKET_TYPE,
                        INDEX_LOCATION), BinaryValue.create(geoHash, CHARSET));
                FetchSet.Response response = riakClient
                        .execute(new FetchSet.Builder(location).build());
                for (BinaryValue binaryValue : response.getDatatype().view()) {
                    result.add(findOne(binaryValue.toString(CHARSET)));
                }
            }
            catch (ExecutionException | InterruptedException e) {
                logger.warn(
                        "Got {} while trying to fetch all messages with geoHash {}: {}",
                        e.getClass().getSimpleName(), geoHash, e.getMessage());
            }
        }
        return result;
    }

    @Override
    public void addOrUpdateIndices(BinaryValue oldValue, Message message,
                                   String id) {
        super.addOrUpdateIndices(oldValue, message, id);
        if (null == oldValue) {
            String geoHash =
                    GeoHash.encodeHash(message.getLocation().getLatitude(),
                            message.getLocation().getLongitude(), 6);
            addKeyToIndex(INDEX_LOCATION, BinaryValue.create(geoHash, CHARSET),
                    BinaryValue.create(id, CHARSET));
            addKeyToIndex(INDEX_USER,
                    BinaryValue.create(message.getUser(), CHARSET),
                    BinaryValue.create(id, CHARSET));
        }
    }

    @Override
    public void removeIndices(Message message, String id) {
        super.removeIndices(message, id);
        String geoHash = GeoHash.encodeHash(message.getLocation().getLatitude(),
                message.getLocation().getLongitude(), 6);
        removeKeyFromIndex(INDEX_LOCATION, BinaryValue.create(geoHash, CHARSET),
                BinaryValue.create(id, CHARSET));
        removeKeyFromIndex(INDEX_USER,
                BinaryValue.create(message.getUser(), CHARSET),
                BinaryValue.create(id, CHARSET));
    }
}
