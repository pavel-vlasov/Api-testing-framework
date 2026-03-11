package com.apiframework.sampledomain.endpoint;

enum PostmanEchoRoute {
    GET_ECHO("/get"),
    POST_ECHO("/post");

    private final String path;

    PostmanEchoRoute(String path) {
        this.path = path;
    }

    String path() {
        return path;
    }
}
