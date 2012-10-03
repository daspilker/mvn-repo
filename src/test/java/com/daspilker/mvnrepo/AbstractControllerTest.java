package com.daspilker.mvnrepo;

import com.mongodb.gridfs.GridFS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

@RunWith(MockitoJUnitRunner.class)
public class AbstractControllerTest {
    @InjectMocks
    private AbstractController controller = new AbstractController() {
    };

    @Mock
    private GridFS releaseGridFS;

    @Mock
    private GridFS snapshotGridFS;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    public void testGetGridFS() {
        assertEquals(releaseGridFS, controller.getGridFS("releases"));
        assertEquals(snapshotGridFS, controller.getGridFS("snapshots"));
    }

    @Test
    public void testGetPath() {
        when(httpServletRequest.getPathInfo()).thenReturn("/bla/foo");
        when(httpServletRequest.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/bla/**");

        assertEquals("foo", AbstractController.getPath(httpServletRequest));
    }
}
