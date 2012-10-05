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
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static java.util.Collections.singletonList;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryControllerTest {
    @InjectMocks
    private RepositoryController repositoryController;

    @Mock
    private GridFS releaseGridFS;

    @Mock
    private GridFS snapshotGridFS;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private GridFSDBFile gridFSDBFile;

    @Mock
    private GridFSInputFile gridFSInputFile;

    @Mock
    private ServletOutputStream servletOutputStream;

    @Mock
    private OutputStream outputStream;

    @Mock
    private ServletInputStream servletInputStream;

    @Test
    public void testGetFile() throws IOException {
        when(httpServletRequest.getPathInfo()).thenReturn("/test.xml");
        when(httpServletRequest.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/**");
        when(releaseGridFS.findOne("test.xml")).thenReturn(gridFSDBFile);
        when(gridFSDBFile.getLength()).thenReturn(4711L);
        when(gridFSDBFile.getMD5()).thenReturn("1234567890");
        when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);

        repositoryController.getFile("releases", httpServletRequest, httpServletResponse);

        verify(releaseGridFS).findOne("test.xml");
        verify(httpServletResponse).setContentLength(4711);
        verify(httpServletResponse).setHeader("Content-MD5", "1234567890");
        verify(gridFSDBFile).writeTo(servletOutputStream);
    }

    @Test
    public void testGetFileNotFound() throws IOException {
        when(httpServletRequest.getPathInfo()).thenReturn("/test.xml");
        when(httpServletRequest.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/**");

        repositoryController.getFile("releases", httpServletRequest, httpServletResponse);

        verify(releaseGridFS).findOne("test.xml");
        verify(httpServletResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testPutFileNew() throws IOException {
        when(httpServletRequest.getPathInfo()).thenReturn("/test.xml");
        when(httpServletRequest.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/**");
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
        when(releaseGridFS.createFile("test.xml")).thenReturn(gridFSInputFile);
        when(gridFSInputFile.getOutputStream()).thenReturn(outputStream);
        when(servletInputStream.read(any(byte[].class))).thenReturn(-1);

        repositoryController.putFile("releases", httpServletRequest, httpServletResponse);

        verify(releaseGridFS).find("test.xml");
        verify(releaseGridFS).createFile("test.xml");
        verify(outputStream).close();
        verify(httpServletResponse).setStatus(SC_CREATED);
    }

    @Test
    public void testPutFileReplace() throws IOException {
        when(httpServletRequest.getPathInfo()).thenReturn("/test.xml");
        when(httpServletRequest.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/**");
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
        when(releaseGridFS.createFile("test.xml")).thenReturn(gridFSInputFile);
        when(releaseGridFS.find("test.xml")).thenReturn(singletonList(gridFSDBFile));
        when(gridFSInputFile.getOutputStream()).thenReturn(outputStream);
        when(servletInputStream.read(any(byte[].class))).thenReturn(-1);

        repositoryController.putFile("releases", httpServletRequest, httpServletResponse);

        verify(releaseGridFS).find("test.xml");
        verify(releaseGridFS).createFile("test.xml");
        verify(releaseGridFS).remove(gridFSDBFile);
        verify(outputStream).close();
        verify(httpServletResponse).setStatus(SC_NO_CONTENT);
    }

    @Test(expected = IOException.class)
    public void testPutFileException() throws IOException {
        when(httpServletRequest.getPathInfo()).thenReturn("/test.xml");
        when(httpServletRequest.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE)).thenReturn("/**");
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
        when(releaseGridFS.createFile("test.xml")).thenReturn(gridFSInputFile);
        when(gridFSInputFile.getOutputStream()).thenReturn(outputStream);
        when(servletInputStream.read(any(byte[].class))).thenThrow(new IOException());

        try {
            repositoryController.putFile("releases", httpServletRequest, httpServletResponse);
        } finally {
            verify(outputStream).close();
        }
    }
}
