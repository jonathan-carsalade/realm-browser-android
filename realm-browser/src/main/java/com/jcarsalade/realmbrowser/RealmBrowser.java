package com.jcarsalade.realmbrowser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import io.realm.Realm;
import io.realm.RealmModel;

public class RealmBrowser {
    private DataRepository dataRepository;
    private Map<Realm, Set<Class<? extends RealmModel>>> realmMap;
    private AsyncHttpServer httpServer;
    private int httpServerPort;

    private RealmBrowser(Map<Realm, Set<Class<? extends RealmModel>>> realmMap, int port) {
        this.realmMap = realmMap;
        this.httpServerPort = port;

        dataRepository = new DataRepository(realmMap);
        httpServer = new AsyncHttpServer();

        httpServer.addAction("GET", "/realms", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                List<RealmJson> realms = dataRepository.getRealms();

                handleHttpResponseFromData(response, realms);
            }
        });

        httpServer.addAction("GET", "/realms/([a-zA-Z0-9]+)", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                Matcher requestMatcher = request.getMatcher();
                RealmJson realm = dataRepository.getRealm(requestMatcher.group(1));

                handleHttpResponseFromData(response, realm);
            }
        });

        httpServer.addAction("GET", "/schema/([a-zA-Z0-9]+)/([a-zA-Z0-9]+)", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                Matcher requestMatcher = request.getMatcher();
                List<FieldJson> schema = dataRepository.getSchema(requestMatcher.group(1), requestMatcher.group(2));

                handleHttpResponseFromData(response, schema);
            }
        });

        httpServer.addAction("GET", "/data/([a-zA-Z0-9]+)/([a-zA-Z0-9]+)", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                Matcher requestMatcher = request.getMatcher();
                List<Map<String, Object>> data = dataRepository.getData(requestMatcher.group(1), requestMatcher.group(2));

                handleHttpResponseFromData(response, data);
            }
        });

        httpServer.listen(port);
    }

    public void stop() {
        httpServer.stop();
    }

    private void handleHttpResponseFromData(AsyncHttpServerResponse response, Object data) {
        if (data == null) {
            response.code(404).end();
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        String result = "";

        try {
            result = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            response.code(500).end();
            return;
        }

        response.send("application/json", result);
    }

    public static class Builder {
        private Map<Realm, Set<Class<? extends RealmModel>>> map;
        private int port = 8888;

        public Builder() {
            map = new HashMap<>();
        }

        public Builder add(Realm realm, Class<? extends RealmModel> clazz) {
            Set<Class<? extends RealmModel>> classes = map.get(realm);

            if (classes == null) {
                classes = new HashSet<>();
                classes.add(clazz);

                map.put(realm, classes);
            } else {
                classes.add(clazz);
            }

            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public RealmBrowser build() {
            return new RealmBrowser(map, port);
        }
    }
}
