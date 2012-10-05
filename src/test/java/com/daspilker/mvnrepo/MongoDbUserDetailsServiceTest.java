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

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MongoDbUserDetailsServiceTest {
    @InjectMocks
    private MongoDbUserDetailsService mongoDbUserDetailsService;

    @Mock
    private MongoDbFactory mongoDbFactory;

    @Mock
    private DB db;

    @Mock
    private DBCollection dbCollection;

    @Mock
    private DBObject dbObject;

    @Before
    public void setUp() {
        when(mongoDbFactory.getDb()).thenReturn(db);
        when(db.getCollection("users")).thenReturn(dbCollection);
    }

    @Test
    public void testInitialize() {
        mongoDbUserDetailsService.initialize();

        verify(dbCollection).ensureIndex(new BasicDBObject("username", 1));
    }

    @Test
    public void testLoadUserByUsername() {
        BasicDBList authorities = new BasicDBList();
        authorities.add("ROLE_OTHER");

        when(dbCollection.findOne(new BasicDBObject("username", "daspilker"))).thenReturn(dbObject);
        when(dbObject.get("password")).thenReturn("secret");
        when(dbObject.get("salt")).thenReturn("salted");
        when(dbObject.get("authorities")).thenReturn(authorities);

        UserDetails result = mongoDbUserDetailsService.loadUserByUsername("daspilker");

        assertNotNull(result);
        assertEquals("daspilker", result.getUsername());
        assertEquals("secret", result.getPassword());
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_OTHER")));
        assertTrue(result instanceof MongoDbUserDetailsService.SaltedUser);
        assertEquals("salted", ((MongoDbUserDetailsService.SaltedUser) result).getSalt());
        verify(dbCollection).findOne(new BasicDBObject("username", "daspilker"));
    }

    @Test
    public void testLoadUserByUsernameNoAuthorities() {
        when(dbCollection.findOne(new BasicDBObject("username", "daspilker"))).thenReturn(dbObject);
        when(dbObject.get("password")).thenReturn("secret");
        when(dbObject.get("salt")).thenReturn("salted");

        UserDetails result = mongoDbUserDetailsService.loadUserByUsername("daspilker");

        assertNotNull(result);
        assertEquals("daspilker", result.getUsername());
        assertEquals("secret", result.getPassword());
        assertTrue(result.getAuthorities().isEmpty());
        assertTrue(result instanceof MongoDbUserDetailsService.SaltedUser);
        assertEquals("salted", ((MongoDbUserDetailsService.SaltedUser) result).getSalt());
        verify(dbCollection).findOne(new BasicDBObject("username", "daspilker"));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testLoadUserByUsernameNotFound() {
        mongoDbUserDetailsService.loadUserByUsername("daspilker");

        verify(dbCollection).findOne(new BasicDBObject("username", "daspilker"));
    }
}
