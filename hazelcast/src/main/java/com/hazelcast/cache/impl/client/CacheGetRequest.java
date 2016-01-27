/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.cache.impl.client;

import com.hazelcast.cache.impl.CacheOperationProvider;
import com.hazelcast.cache.impl.CachePortableHook;
import com.hazelcast.cache.impl.operation.CacheGetOperation;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.security.permission.ActionConstants;
import com.hazelcast.security.permission.CachePermission;
import com.hazelcast.spi.Operation;

import javax.cache.expiry.ExpiryPolicy;
import java.io.IOException;
import java.security.Permission;

/**
 * This client request  specifically calls {@link CacheGetOperation} on the server side.
 *
 * @see com.hazelcast.cache.impl.operation.CacheGetOperation
 */
public class CacheGetRequest
        extends AbstractCacheRequest {

    private Data key;
    private ExpiryPolicy expiryPolicy;

    public CacheGetRequest() {
    }

    public CacheGetRequest(String name, Data key, ExpiryPolicy expiryPolicy, InMemoryFormat inMemoryFormat) {
        super(name, inMemoryFormat);
        this.key = key;
        this.expiryPolicy = expiryPolicy;
    }

    @Override
    public int getClassId() {
        return CachePortableHook.GET;
    }

    @Override
    protected Object getKey() {
        return key;
    }

    @Override
    protected Operation prepareOperation() {
        CacheOperationProvider operationProvider = getOperationProvider();
        return operationProvider.createGetOperation(key, expiryPolicy);
    }

    @Override
    public void write(PortableWriter writer)
            throws IOException {
        super.write(writer);
        final ObjectDataOutput out = writer.getRawDataOutput();
        out.writeData(key);
        out.writeObject(expiryPolicy);
    }

    @Override
    public void read(PortableReader reader)
            throws IOException {
        super.read(reader);
        final ObjectDataInput in = reader.getRawDataInput();
        key = in.readData();
        this.expiryPolicy = in.readObject();
    }

    @Override
    public Permission getRequiredPermission() {
        return new CachePermission(name, ActionConstants.ACTION_READ);
    }

    @Override
    public Object[] getParameters() {
        if (expiryPolicy == null) {
            return new Object[]{key};
        }
        return new Object[]{key, expiryPolicy};
    }

    @Override
    public String getMethodName() {
        return "get";
    }

    @Override
    public String getDistributedObjectName() {
        return name;
    }

}