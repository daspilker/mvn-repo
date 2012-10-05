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
import com.mongodb.gridfs.GridFS;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static com.mongodb.BasicDBObjectBuilder.start;

@Configuration
public class WebAppConfiguration extends WebMvcConfigurerAdapter {
    private static final String FUNCTIONS_PACKAGE = "com/daspilker/mvnrepo/functions/";

    @Inject
    private MongoDbFactory mongoDbFactory;

    @PostConstruct
    public void initialize() throws IOException {
        loadFunction(mongoDbFactory.getDb(), "sha256");
        loadFunction(mongoDbFactory.getDb(), "createUser");
    }

    @Bean
    public GridFS releaseGridFS() {
        return new GridFS(mongoDbFactory.getDb(), "releases");
    }

    @Bean
    public GridFS snapshotGridFS() {
        return new GridFS(mongoDbFactory.getDb(), "snapshots");
    }

    static String loadFunction(String name) throws IOException {
        return Resources.toString(getResource(FUNCTIONS_PACKAGE + name + ".js"), UTF_8);
    }

    private static void loadFunction(DB db, String name) throws IOException {
        db.getCollection("system.js").save(start().add("_id", name).add("value", loadFunction(name)).get());
    }
}
