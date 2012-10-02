package com.daspilker.mvnrepo;

import com.mongodb.gridfs.GridFS;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

public class AbstractController {
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    @Inject
    @Named("releaseGridFS")
    private GridFS releaseGridFS;

    @Inject
    @Named("snapshotGridFS")
    private GridFS snapshotGridFS;

    protected GridFS getGridFS(String repository) {
        return "releases".equals(repository) ? releaseGridFS : snapshotGridFS;
    }

    protected static String getPath(HttpServletRequest request) {
        String pattern = (String) request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        return PATH_MATCHER.extractPathWithinPattern(pattern, request.getPathInfo());
    }
}
