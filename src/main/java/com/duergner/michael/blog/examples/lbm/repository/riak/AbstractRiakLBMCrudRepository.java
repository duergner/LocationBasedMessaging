/*
 * AbstractRiakLBMCrudRepository.java
 *
 * Copyright (C) 2014 Michael Duergner <michael@duergner.com>
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.duergner.michael.blog.examples.lbm.repository.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.FetchDatatype;
import com.basho.riak.client.api.commands.FetchSet;
import com.basho.riak.client.api.commands.UpdateSet;
import com.basho.riak.client.api.commands.datatypes.SetUpdate;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.basho.riak.client.core.util.BinaryValue;
import com.duergner.michael.blog.examples.lbm.model.Base;
import com.duergner.michael.blog.examples.lbm.repository.LBMCrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Repository;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Michael Duergner <michael@duergner.com>
 */
@Repository
public abstract class AbstractRiakLBMCrudRepository<T extends Base>
        implements LBMCrudRepository<T>, ApplicationContextAware,
        BeanNameAware {

    Logger logger = LoggerFactory.getLogger(getClass());

    static final Charset CHARSET = Charset.forName("UTF-8");

    static final BinaryValue SET_BUCKET_TYPE =
            BinaryValue.create("set_bucket", CHARSET);

    ApplicationContext applicationContext;

    String beanName;

    @Autowired
    RiakClient riakClient;

    Class<T> clazz;

    AbstractRiakLBMCrudRepository(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void setApplicationContext(
            ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> S save(S entity) {
        BinaryValue oldValue = entity.binaryValue;
        String id = entity.getId();
        StoreValue.Builder builder = new StoreValue.Builder(entity).withOption(
                StoreValue.Option.RETURN_BODY,Boolean.TRUE);
        if (null == id) {
            builder.withNamespace(namespace());
        }
        else {
            builder.withLocation(location(entity));
        }
        if (null != entity.vClock) {
            builder.withVectorClock(entity.vClock);
        }
        StoreValue storeValue = builder.build();
        try {
            StoreValue.Response storeValueResponse =
                    riakClient.execute(storeValue);
            entity = (S) storeValueResponse.getValue(entity.getClass());
            if (storeValueResponse.hasGeneratedKey()) {
                entity.setId(
                        storeValueResponse.getGeneratedKey().toString(CHARSET));
            }
            else {
                entity.setId(id);
            }
            getProxy().addOrUpdateIndices(oldValue, entity, entity.getId());
            entity.vClock = storeValueResponse.getVectorClock();
            entity.binaryValue =
                    storeValueResponse.getValue(RiakObject.class).getValue();
            return entity;
        }
        catch (ExecutionException | InterruptedException e) {
            throw new RecoverableDataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public <S extends T> Iterable<S> save(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public T findOne(String id) {
        FetchValue fetchValue = new FetchValue.Builder(location(id)).build();
        try {
            FetchValue.Response fetchValueResponse =
                    riakClient.execute(fetchValue);
            if (!fetchValueResponse.isNotFound()) {
                T result = fetchValueResponse.getValue(clazz);
                result.setId(id);
                result.vClock = fetchValueResponse.getVectorClock();
                result.binaryValue =
                        fetchValueResponse.getValue(RiakObject.class)
                                .getValue();
                return result;
            }
            else {
                return null;
            }
        }
        catch (ExecutionException | InterruptedException e) {
            throw new RecoverableDataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String id) {
        FetchValue fetchValue = new FetchValue.Builder(location(id))
                .withOption(FetchValue.Option.HEAD, Boolean.TRUE).build();
        try {
            FetchValue.Response fetchValueResponse =
                    riakClient.execute(fetchValue);
            return !fetchValueResponse.isNotFound();
        }
        catch (ExecutionException | InterruptedException e) {
            throw new RecoverableDataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public Iterable<T> findAll() {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    public Iterable<T> findAll(Iterable<String> strings) {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    public long count() {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    public void delete(String id) {
        delete(findOne(id));
    }

    @Override
    public void delete(T entity) {
        DeleteValue deleteValue = new DeleteValue.Builder(location(entity.getId())).build();
        try {
            riakClient.execute(deleteValue);
            getProxy().removeIndices(entity,entity.getId());
        }
        catch (ExecutionException | InterruptedException e) {
            throw new RecoverableDataAccessException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        throw new RuntimeException("Not yet implemented!");
    }

    public void addOrUpdateIndices(BinaryValue oldValue, T entity, String id) {
        // This is just a noop implementation; override this on your subclass whenever you want to add this object to an index
    }

    public void removeIndices(T entity, String id) {
        // This is just a noop implementation; override this on your subclass whenever you need to remove this object from an index
    }

    @SuppressWarnings("unchecked")
    protected AbstractRiakLBMCrudRepository<T> getProxy() {
        return applicationContext
                .getBean(beanName, AbstractRiakLBMCrudRepository.class);
    }

    Namespace namespace() {
        return new Namespace(clazz.getSimpleName(), CHARSET);
    }

    Location location(String id) {
        return new Location(namespace(), id, CHARSET);
    }

    Location location(T entity) {
        return location(entity.getId());
    }

    void addKeyToIndex(BinaryValue indexName, BinaryValue indexKey,
                       BinaryValue indexValue) {
        riakClient.executeAsync(new UpdateSet.Builder(new Location(
                new Namespace(AbstractRiakLBMCrudRepository.SET_BUCKET_TYPE,
                        indexName), indexKey), new SetUpdate().add(indexValue))
                .build());
    }

    void removeKeyFromIndex(BinaryValue indexName, BinaryValue indexKey,
                            BinaryValue indexValue) {
        try {
            Location location = new Location(
                    new Namespace(AbstractRiakLBMCrudRepository.SET_BUCKET_TYPE,
                            indexName), indexKey);
            FetchSet.Response response = riakClient.execute(
                    new FetchSet.Builder(location)
                            .withOption(FetchDatatype.Option.INCLUDE_CONTEXT,
                                    Boolean.TRUE).build());
            if (null != response.getContext()) {
                riakClient.executeAsync(new UpdateSet.Builder(location,
                        new SetUpdate().remove(indexValue))
                        .withContext(response.getContext()).build());
            }
        }
        catch (ExecutionException | InterruptedException e) {
            logger.warn("Got {} while trying to removeKeyFromIndex: {}",
                    e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
