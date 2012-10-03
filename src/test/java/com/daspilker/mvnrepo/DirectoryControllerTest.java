package com.daspilker.mvnrepo;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.gridfs.GridFS;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static com.google.common.collect.Iterables.elementsEqual;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryControllerTest {
    @InjectMocks
    private DirectoryController directoryController;

    @Mock
    private GridFS releaseGridFS;

    @Mock
    private GridFS snapshotGridFS;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private DB db;

    @Mock
    private DBCollection dbCollection;

    @Mock
    private MapReduceOutput mapReduceOutput;

    @Mock
    private DBObject fileDBObject;

    @Mock
    private DBObject directoryDBObject;

    @Before
    public void setUp() throws IOException {
        directoryController.initialize();
        when(releaseGridFS.getDB()).thenReturn(db);
        when(releaseGridFS.getBucketName()).thenReturn("releases");
        when(snapshotGridFS.getDB()).thenReturn(db);
        when(snapshotGridFS.getBucketName()).thenReturn("snapshots");
        when(db.getCollection("releases.files")).thenReturn(dbCollection);
        when(db.getCollection("snapshots.files")).thenReturn(dbCollection);
        when(dbCollection.mapReduce(any(MapReduceCommand.class))).thenReturn(mapReduceOutput);
        when(fileDBObject.get("value")).thenReturn(false);
        when(directoryDBObject.get("value")).thenReturn(true);
    }

    @Test
    public void testGetDirectoryNoTrailingSlash() throws IOException {
        when(httpServletRequest.getPathInfo()).thenReturn("/test");
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://test.org/test"));

        ModelAndView result = directoryController.getDirectory("releases", httpServletRequest, httpServletResponse);

        assertNull(result);
        verify(httpServletResponse).sendRedirect("http://test.org/test/");
    }

    @Test
    public void testGetDirectoryNotFound() throws IOException {
        when(httpServletRequest.getPathInfo()).thenReturn("/test/");
        when(httpServletRequest.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/**");
        when(mapReduceOutput.results()).thenReturn(Collections.<DBObject>emptyList());

        ModelAndView result = directoryController.getDirectory("releases", httpServletRequest, httpServletResponse);

        assertNull(result);
        verify(httpServletResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testGetDirectoryRoot() throws IOException {
        when(httpServletRequest.getPathInfo()).thenReturn("/");
        when(httpServletRequest.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/**");
        when(mapReduceOutput.results()).thenReturn(asList(fileDBObject, directoryDBObject, null));

        ModelAndView result = directoryController.getDirectory("releases", httpServletRequest, httpServletResponse);

        assertNotNull(result);
        assertEquals("directory", result.getViewName());
        assertEquals(4, result.getModel().size());
        assertEquals("Release", result.getModel().get("repository"));
        assertEquals("", result.getModel().get("path"));
        assertTrue(elementsEqual(asList(fileDBObject), (Iterable) result.getModel().get("files")));
        assertTrue(elementsEqual(asList(directoryDBObject), (Iterable) result.getModel().get("directories")));
    }

    @Test
    public void testGetDirectory() throws IOException {
        when(httpServletRequest.getPathInfo()).thenReturn("/foo/");
        when(httpServletRequest.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/**");
        when(mapReduceOutput.results()).thenReturn(asList(fileDBObject, directoryDBObject, null));

        ModelAndView result = directoryController.getDirectory("snapshots", httpServletRequest, httpServletResponse);

        assertNotNull(result);
        assertEquals("directory", result.getViewName());
        assertEquals(4, result.getModel().size());
        assertEquals("Snapshot", result.getModel().get("repository"));
        assertEquals("foo/", result.getModel().get("path"));
        assertTrue(elementsEqual(asList(fileDBObject), (Iterable) result.getModel().get("files")));
        assertTrue(elementsEqual(asList(directoryDBObject), (Iterable) result.getModel().get("directories")));
    }
}
