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
import org.bson.types.BasicBSONList;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.LinkedList;

import static com.mongodb.BasicDBObjectBuilder.start;

@Component
public class MongoDbUserDetailsService implements UserDetailsService {
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_AUTHORITIES = "authorities";
    private static final String FIELD_SALT = "salt";

    @Inject
    private DB db;

    @PostConstruct
    public void initialize() {
        getCollection().ensureIndex(start().add(FIELD_USERNAME, 1).get());
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        DBObject user = getCollection().findOne(start().add(FIELD_USERNAME, username).get());
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        String password = (String) user.get(FIELD_PASSWORD);
        Collection<GrantedAuthority> authorities = convertAuthorities((BasicBSONList) user.get(FIELD_AUTHORITIES));
        Object salt = user.get(FIELD_SALT);
        return new SaltedUser(username, password, authorities, salt);
    }

    private DBCollection getCollection() {
        return db.getCollection("users");
    }

    private static Collection<GrantedAuthority> convertAuthorities(BasicBSONList authorities) {
        Collection<GrantedAuthority> result = new LinkedList<>();
        if (authorities != null) {
            for (Object authority : authorities) {
                result.add(new SimpleGrantedAuthority((String) authority));
            }
        }
        return result;
    }

    public static final class SaltedUser extends User {
        private static final long serialVersionUID = -5440899816444344051L;

        private Object salt;

        public SaltedUser(String username, String password, Collection<GrantedAuthority> authorities, Object salt) {
            super(username, password, authorities);
            this.salt = salt;
        }

        public Object getSalt() {
            return salt;
        }
    }
}
