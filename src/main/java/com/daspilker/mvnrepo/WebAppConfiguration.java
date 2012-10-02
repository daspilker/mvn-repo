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

import com.google.common.io.Resources;
import com.mongodb.DB;
import com.mongodb.MongoURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static com.mongodb.BasicDBObjectBuilder.start;

@EnableWebMvc
@Configuration
public class WebAppConfiguration extends WebMvcConfigurerAdapter {
    private static final String FUNCTIONS_PACKAGE = "com/daspilker/mvnrepo/functions/";

    @Value("${MONGOLAB_URI:mongodb://localhost:27017/mvnrepo}")
    private String mongoUri;

    @Bean
    public DB db() throws IOException {
        MongoURI mongoURI = new MongoURI(mongoUri);
        DB db = mongoURI.connectDB();
        if (mongoURI.getUsername() != null && mongoURI.getPassword() != null) {
            db.authenticate(mongoURI.getUsername(), mongoURI.getPassword());
        }

        String sha256Function = Resources.toString(getResource(FUNCTIONS_PACKAGE + "sha256.js"), UTF_8);
        db.getCollection("system.js").save(start().add("_id", "sha256").add("value", sha256Function).get());
        String createUserFunction = Resources.toString(getResource(FUNCTIONS_PACKAGE + "createUser.js"), UTF_8);
        db.getCollection("system.js").save(start().add("_id", "createUser").add("value", createUserFunction).get());

        return db;
    }
}
