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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.By.linkText;
import static org.openqa.selenium.By.tagName;

public class IndexPageIT {
    private static final String BASE_URL = "http://localhost:8080/";

    private WebDriver driver;

    @Before
    public void setUp() {
        driver = new FirefoxDriver();
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void test() {
        driver.get(BASE_URL);

        WebElement headline = driver.findElement(tagName("h1"));
        assertEquals("Maven Repository Browser", headline.getText());

        WebElement releasesLink = driver.findElement(linkText("Browse Release Repository"));
        assertEquals(BASE_URL + "browser/releases/", releasesLink.getAttribute("href"));

        WebElement snapshotsLink = driver.findElement(linkText("Browse Snapshot Repository"));
        assertEquals(BASE_URL + "browser/snapshots/", snapshotsLink.getAttribute("href"));
    }
}
