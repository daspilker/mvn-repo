/*
   Copyright 2012 Daniel A. Spilker

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.daspilker.mvnrepo;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.MongoDbFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebAppConfigurationTest {
    @InjectMocks
    private WebAppConfiguration configuration;

    @Mock
    private MongoDbFactory mongoDbFactory;

    @Mock
    private DB db;

    @Mock
    private DBCollection dbCollection;

    @Mock
    private DBCollection dbCollection2;

    @Before
    public void setUp() {
        when(mongoDbFactory.getDb()).thenReturn(db);
    }

    @Test
    public void testInitialize() throws IOException {
        when(db.getCollection("system.js")).thenReturn(dbCollection);

        configuration.initialize();

        verify(dbCollection, times(2)).save(any(DBObject.class));
    }

    @Test
    public void testReleaseGridFS() {
        when(db.getCollection("releases.files")).thenReturn(dbCollection);
        when(db.getCollection("releases.chunks")).thenReturn(dbCollection2);

        GridFS result = configuration.releaseGridFS();

        assertNotNull(result);
        assertEquals("releases", result.getBucketName());
        assertSame(db, result.getDB());
    }

    @Test
    public void testSnapshotGridFS() {
        when(db.getCollection("snapshots.files")).thenReturn(dbCollection);
        when(db.getCollection("snapshots.chunks")).thenReturn(dbCollection2);

        GridFS result = configuration.snapshotGridFS();

        assertNotNull(result);
        assertEquals("snapshots", result.getBucketName());
        assertSame(db, result.getDB());
    }
}
