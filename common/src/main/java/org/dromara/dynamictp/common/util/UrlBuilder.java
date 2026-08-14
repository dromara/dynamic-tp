/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight URL builder for constructing and modifying URLs.
 * <p>
 * Extracted from Hutool's UrlBuilder to remove the Hutool dependency.
 * Only supports the subset of features used by dynamic-tp notifiers:
 * parsing an existing URL, reading/adding query params, reading/adding path segments,
 * and rebuilding the final URL string.
 *
 * @author dynamic-tp
 * @since 1.2.3
 */
public final class UrlBuilder {

    /** scheme://host[:port] part, e.g. "https://oapi.dingtalk.com" */
    private final String origin;

    private final List<String> pathSegments = new ArrayList<>();

    private final Map<String, String> queryParams = new LinkedHashMap<>();

    private UrlBuilder(String url) {
        int queryIndex = url.indexOf('?');
        String urlWithoutQuery = queryIndex >= 0 ? url.substring(0, queryIndex) : url;

        // split origin and path
        int schemeEnd = urlWithoutQuery.indexOf("://");
        int hostStart = schemeEnd >= 0 ? schemeEnd + 3 : 0;
        int slashIndex = urlWithoutQuery.indexOf('/', hostStart);
        if (slashIndex >= 0) {
            origin = urlWithoutQuery.substring(0, slashIndex);
            parsePath(urlWithoutQuery.substring(slashIndex + 1));
        } else {
            origin = urlWithoutQuery;
        }

        if (queryIndex >= 0) {
            parseQuery(url.substring(queryIndex + 1));
        }
    }

    /**
     * Create a UrlBuilder from an existing URL string.
     *
     * @param url the URL to parse
     * @return a new UrlBuilder
     */
    public static UrlBuilder of(String url) {
        return new UrlBuilder(url);
    }

    private void parsePath(String path) {
        for (String segment : path.split("/")) {
            if (!segment.isEmpty()) {
                pathSegments.add(segment);
            }
        }
    }

    private void parseQuery(String query) {
        if (query.isEmpty()) {
            return;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                queryParams.put(pair.substring(0, eq), pair.substring(eq + 1));
            } else if (!pair.isEmpty()) {
                queryParams.put(pair, "");
            }
        }
    }

    /**
     * Get a query parameter value by key.
     *
     * @param key query parameter name
     * @return the value, or null if not present
     */
    public String getQueryParam(String key) {
        return queryParams.get(key);
    }

    /**
     * Add a query parameter. If the key already exists, its value is replaced.
     *
     * @param key   query parameter name
     * @param value query parameter value
     * @return this builder for chaining
     */
    public UrlBuilder addQuery(String key, Object value) {
        queryParams.put(key, String.valueOf(value));
        return this;
    }

    /**
     * Get the path segments.
     *
     * @return unmodifiable list of path segments
     */
    public List<String> getPathSegments() {
        return Collections.unmodifiableList(pathSegments);
    }

    /**
     * Append a path segment.
     *
     * @param segment the path segment to add
     * @return this builder for chaining
     */
    public UrlBuilder addPath(String segment) {
        pathSegments.add(segment);
        return this;
    }

    /**
     * Rebuild the full URL string from the current state.
     *
     * @return the full URL
     */
    public String build() {
        StringBuilder sb = new StringBuilder(origin);
        for (String segment : pathSegments) {
            sb.append('/').append(segment);
        }
        if (!queryParams.isEmpty()) {
            sb.append('?');
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    sb.append('&');
                }
                sb.append(entry.getKey()).append('=').append(entry.getValue());
                first = false;
            }
        }
        return sb.toString();
    }
}
