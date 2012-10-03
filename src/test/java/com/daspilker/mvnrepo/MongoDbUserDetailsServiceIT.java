package com.daspilker.mvnrepo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoURI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.net.UnknownHostException;

import static com.mongodb.BasicDBObjectBuilder.start;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MongoDbUserDetailsServiceIT.TestConfiguration.class)
public class MongoDbUserDetailsServiceIT {
    @Inject
    private UserDetailsService userDetailsService;

    @Inject
    private DB db;

    @Before
    public void setUp() {
        db.getCollection("users").remove(new BasicDBObject("username", "daspilker"));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testNotFound() {
        userDetailsService.loadUserByUsername("daspilker");
    }

    @Test
    public void test() {
        DBObject user = start().
                add("username", "daspilker").
                add("password", "secret").
                add("authorities", singletonList("ROLE_TEST")).
                add("salt", "salted").
                get();
        db.getCollection("users").insert(user);

        UserDetails result = userDetailsService.loadUserByUsername("daspilker");

        assertNotNull(result);
        assertEquals("daspilker", result.getUsername());
        assertEquals("secret", result.getPassword());
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEST")));
        assertTrue(result instanceof MongoDbUserDetailsService.SaltedUser);
        assertEquals("salted", ((MongoDbUserDetailsService.SaltedUser) result).getSalt());
    }

    @Configuration
    public static class TestConfiguration {
        @Bean
        public DB db() throws UnknownHostException {
            return new MongoURI("mongodb://localhost/mvnrepo-test").connectDB();
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return new MongoDbUserDetailsService();
        }
    }
}
