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
    private DB db;

    @Mock
    private DBCollection dbCollection;

    @Mock
    private DBObject dbObject;

    @Before
    public void setUp() {
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