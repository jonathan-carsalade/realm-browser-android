package com.jcarsalade.realmbrowser;

import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmModel;

public class RealmBrowser {
    private Map<Realm, Set<Class<? extends RealmModel>>> realmMap;
    private AsyncHttpServer httpServer;
    private int httpServerPort;

    private RealmBrowser(Map<Realm, Set<Class<? extends RealmModel>>> realmMap, int port) {
        this.realmMap = realmMap;
        this.httpServerPort = port;

        httpServer = new AsyncHttpServer();

        httpServer.addAction("GET", "/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                response.send("GET request from Android!");
            }
        });

        httpServer.listen(port);
    }

    public static class Builder {
        private Map<Realm, Set<Class<? extends RealmModel>>> map;
        private int port = 8888;

        public Builder() {
            map = new HashMap<>();
        }

        public Builder add(Realm realm, final Class<? extends RealmModel> clazz) {
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
