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
