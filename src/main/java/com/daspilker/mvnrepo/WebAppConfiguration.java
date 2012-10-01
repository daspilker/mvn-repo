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
import com.mongodb.MongoURI;
import com.mongodb.gridfs.GridFS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.net.UnknownHostException;

@EnableWebMvc
@Configuration
public class WebAppConfiguration extends WebMvcConfigurerAdapter {
    @Value("${MONGOLAB_URI:mongodb://localhost:27017/mvnrepo}")
    private String mongoUri;

    @Bean
    public DB db() throws UnknownHostException {
        MongoURI mongoURI = new MongoURI(mongoUri);
        DB db = mongoURI.connectDB();
        if (mongoURI.getUsername() != null && mongoURI.getPassword() != null) {
            db.authenticate(mongoURI.getUsername(), mongoURI.getPassword());
        }
        return db;
    }

    @Bean
    public GridFS gridFS() throws UnknownHostException {
        return new GridFS(db());
    }
}
