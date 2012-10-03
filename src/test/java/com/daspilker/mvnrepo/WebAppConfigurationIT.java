package com.daspilker.mvnrepo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebAppConfiguration.class, WebAppConfigurationIT.TestConfiguration.class})
public class WebAppConfigurationIT {
    @Inject
    private BeanFactory beanFactory;

    @Test
    public void testDb() {
        DB db = beanFactory.getBean(DB.class);

        assertNotNull(db);
        assertNotNull(db.getCollection("system.js").findOne(new BasicDBObject("_id", "sha256")));
        assertNotNull(db.getCollection("system.js").findOne(new BasicDBObject("_id", "createUser")));
    }

    @Test
    public void testReleasesGridFS() {
        GridFS releaseGridFS = beanFactory.getBean("releaseGridFS", GridFS.class);

        assertNotNull(releaseGridFS);
        assertEquals("releases", releaseGridFS.getBucketName());
    }

    @Test
    public void testSnapshotsGridFS() {
        GridFS snapshotGridFS = beanFactory.getBean("snapshotGridFS", GridFS.class);

        assertNotNull(snapshotGridFS);
        assertEquals("snapshots", snapshotGridFS.getBucketName());
    }

    @Configuration
    public static class TestConfiguration {
        @Bean
        public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
            Properties properties = new Properties();
            properties.put("MONGOLAB_URI", "mongodb://localhost:27017/mvnrepo-test");
            PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
            propertyPlaceholderConfigurer.setProperties(properties);
            return propertyPlaceholderConfigurer;
        }
    }
}
