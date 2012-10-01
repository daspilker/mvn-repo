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
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.apache.commons.io.IOUtils.copy;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

@Controller
@RequestMapping(value = "/repository/**")
public class RepositoryController {
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    @Inject
    private GridFS gridFS;

    @RequestMapping(method = GET)
    public void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        GridFSDBFile file = gridFS.findOne(getPath(request));
        if (file == null) {
            response.sendError(SC_NOT_FOUND);
        } else {
            response.setContentLength((int) file.getLength());
            response.setHeader("Content-MD5", file.getMD5());
            file.writeTo(response.getOutputStream());
        }
    }

    @RequestMapping(method = PUT)
    public void putFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = getPath(request);
        List<GridFSDBFile> oldFiles = gridFS.find(path);
        GridFSInputFile file = gridFS.createFile(path);
        try (OutputStream outputStream = file.getOutputStream()) {
            copy(request.getInputStream(), outputStream);
        }
        for (GridFSDBFile oldFile : oldFiles) {
            gridFS.remove(oldFile);
        }
    }

    private static String getPath(HttpServletRequest request) {
        String pattern = (String) request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        return PATH_MATCHER.extractPathWithinPattern(pattern, request.getPathInfo());
    }
}
