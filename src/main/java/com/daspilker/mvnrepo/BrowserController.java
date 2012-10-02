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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.gridfs.GridFS;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.daspilker.mvnrepo.WebAppConfiguration.loadFunction;
import static com.google.common.collect.Iterables.filter;
import static com.mongodb.BasicDBObjectBuilder.start;
import static java.util.regex.Pattern.compile;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(value = "/browser/{repository:releases|snapshots}/**")
public class BrowserController extends AbstractController {
    private static final Predicate<DBObject> FILE_PREDICATE = new FilePredicate(false);
    private static final Predicate<DBObject> DIRECTORY_PREDICATE = new FilePredicate(true);

    private String mapDirectoryFunction;
    private String reduceDirectoryFunction;

    @PostConstruct
    public void initialize() throws IOException {
        mapDirectoryFunction = loadFunction("mapDirectory");
        reduceDirectoryFunction = loadFunction("reduceDirectory");
    }

    @RequestMapping(method = GET)
    public ModelAndView index(@PathVariable String repository,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        if (!request.getPathInfo().endsWith("/")) {
            response.sendRedirect(request.getRequestURL().toString() + "/");
            return null;
        }

        String path = getPath(request);
        if (path.length() > 0) {
            path += "/";
        }

        Iterable<DBObject> listing = getDirectoryListing(repository, path);
        if (Iterables.isEmpty(listing)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("path", path);
        model.put("directories", filter(listing, DIRECTORY_PREDICATE));
        model.put("files", filter(listing, FILE_PREDICATE));
        return new ModelAndView("index", model);
    }

    private Iterable<DBObject> getDirectoryListing(String repository, String path) {
        GridFS gridFS = getGridFS(repository);
        DBCollection collection = gridFS.getDB().getCollection(gridFS.getBucketName() + ".files");
        MapReduceCommand command = new MapReduceCommand(
                collection,
                mapDirectoryFunction,
                reduceDirectoryFunction,
                null,
                MapReduceCommand.OutputType.INLINE,
                start().add("filename", compile("^" + path)).get()
        );
        command.setScope(ImmutableMap.<String, Object>of("path", path));
        return collection.mapReduce(command).results();
    }

    private static class FilePredicate implements Predicate<DBObject> {
        private Boolean value;

        public FilePredicate(boolean value) {
            this.value = value;
        }

        @Override
        public boolean apply(DBObject input) {
            return value.equals(input.get("value"));
        }
    }
}
