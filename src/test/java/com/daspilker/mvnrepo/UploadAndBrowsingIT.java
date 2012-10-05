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

import com.mongodb.DBObject;
import com.mongodb.MongoURI;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.net.UnknownHostException;
import java.util.List;

import static com.mongodb.BasicDBObjectBuilder.start;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.By.linkText;
import static org.openqa.selenium.By.tagName;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = UploadAndBrowsingIT.TestConfiguration.class)
public class UploadAndBrowsingIT {
    private static final String BASE_URL = "http://localhost:8080/";

    @Inject
    private MongoDbFactory mongoDbFactory;

    @Inject
    private WebDriver webDriver;

    @Test
    public void testUploadAndBrowsing() {
        createUser();
        uploadFile();
        browse();
    }

    private void createUser() {
        mongoDbFactory.getDb().getCollection("users").remove(start("username", "daspilker").get());

        String salt = "salt";
        String password = new ShaPasswordEncoder(256).encodePassword("secret", salt);

        DBObject user = start("username", "daspilker").
                add("password", password).
                add("authorities", asList("ROLE_USER")).
                add("salt", salt).
                get();
        mongoDbFactory.getDb().getCollection("users").insert(user);
    }

    private void uploadFile() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Basic " + Utf8.decode(Base64.encode(Utf8.encode("daspilker:secret"))));
        HttpEntity<String> httpEntity = new HttpEntity<>("test", httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(BASE_URL + "repository/releases/com/daspilker/test.txt", httpEntity);

        GridFS gridFS = new GridFS(mongoDbFactory.getDb(), "releases");
        List<GridFSDBFile> files = gridFS.find("com/daspilker/test.txt");

        assertEquals(1, files.size());
        assertEquals(4, files.get(0).getLength());
        assertEquals("098f6bcd4621d373cade4e832627b4f6", files.get(0).getMD5());
    }

    private void browse() {
        webDriver.get(BASE_URL + "browser/releases/");
        browse("/", "com/daspilker/test.txt");
    }

    private void browse(String currentPath, String nextPath) {
        WebElement headline = webDriver.findElement(tagName("h1"));
        assertEquals("Maven Repository Browser", headline.getText());

        WebElement repository = webDriver.findElement(tagName("h2"));
        assertEquals("Release Repository", repository.getText());

        WebElement path = webDriver.findElement(tagName("h3"));
        assertEquals(currentPath, path.getText());

        int pos = nextPath.indexOf("/");
        if (pos > -1) {
            String link = nextPath.substring(0, pos + 1);
            WebElement directory = webDriver.findElement(linkText(link));
            assertEquals(BASE_URL + "browser/releases" + currentPath + link, directory.getAttribute("href"));
            directory.click();
            browse(currentPath + link, nextPath.substring(pos + 1));
        } else {
            String fileName = nextPath.substring(pos + 1);
            List<WebElement> listItems = webDriver.findElements(tagName("li"));
            boolean found = false;
            for (WebElement listItem : listItems) {
                if (fileName.equals(listItem.getText())) {
                    found = true;
                }
            }
            assertTrue(found);
        }
    }

    @Configuration
    public static class TestConfiguration {
        @Bean
        public MongoDbFactory mongoDbFactory() throws UnknownHostException {
            return new SimpleMongoDbFactory(new MongoURI("mongodb://localhost/mvnrepo-test"));
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return new MongoDbUserDetailsService();
        }

        @Bean(destroyMethod = "quit")
        public WebDriver webDriver() {
            return new FirefoxDriver();
        }
    }
}
