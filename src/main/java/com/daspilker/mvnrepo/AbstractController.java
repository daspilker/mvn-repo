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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

public abstract class AbstractController {
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    @Autowired
    @Qualifier("releaseGridFS")
    private GridFS releaseGridFS;

    @Autowired
    @Qualifier("snapshotGridFS")
    private GridFS snapshotGridFS;

    protected GridFS getGridFS(String repository) {
        return "releases".equals(repository) ? releaseGridFS : snapshotGridFS;
    }

    protected static String getPath(HttpServletRequest request) {
        String pattern = (String) request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        return PATH_MATCHER.extractPathWithinPattern(pattern, request.getPathInfo());
    }
}
