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
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static com.google.common.io.ByteStreams.copy;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

@Controller
@RequestMapping(value = "/repository/{repository:releases|snapshots}/**")
public class RepositoryController {
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    private GridFS releaseGridFs;
    private GridFS snapshotGridFs;

    @Inject
    private DB db;

    @PostConstruct
    public void initialize() {
        releaseGridFs = new GridFS(db, "releases");
        snapshotGridFs = new GridFS(db, "snapshots");
    }

    @RequestMapping(method = GET)
    public void getFile(@PathVariable String repository, HttpServletRequest request, HttpServletResponse response) throws IOException {
        GridFSDBFile file = getGridFS(repository).findOne(getPath(request));
        if (file == null) {
            response.sendError(SC_NOT_FOUND);
        } else {
            response.setContentLength((int) file.getLength());
            response.setHeader("Content-MD5", file.getMD5());
            file.writeTo(response.getOutputStream());
        }
    }

    @RequestMapping(method = PUT)
    public void putFile(@PathVariable String repository, HttpServletRequest request, HttpServletResponse response) throws IOException {
        GridFS gridFS = getGridFS(repository);
        String path = getPath(request);
        List<GridFSDBFile> oldFiles = gridFS.find(path);
        GridFSInputFile file = gridFS.createFile(path);
        try (OutputStream outputStream = file.getOutputStream()) {
            copy(request.getInputStream(), outputStream);
        }
        for (GridFSDBFile oldFile : oldFiles) {
            gridFS.remove(oldFile);
        }
        if (oldFiles.isEmpty()) {
            response.setStatus(SC_CREATED);
        } else {
            response.setStatus(SC_NO_CONTENT);
        }
    }

    private GridFS getGridFS(String repository) {
        return "releases".equals(repository) ? releaseGridFs : snapshotGridFs;
    }

    private static String getPath(HttpServletRequest request) {
        String pattern = (String) request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        return PATH_MATCHER.extractPathWithinPattern(pattern, request.getPathInfo());
    }
}
