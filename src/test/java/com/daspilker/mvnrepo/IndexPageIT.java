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
