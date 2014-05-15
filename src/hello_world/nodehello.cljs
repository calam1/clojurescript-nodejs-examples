(ns hello-world.nodehello
(:require [cljs.nodejs :as node]
          [cljs.core :as cljs]))

(def times-greeted (atom {}))

(defn say-hello [request response next]
  (let [name (aget request "params" "name")
        old-count (@times-greeted name)
        new-count (inc (if (nil? old-count) 0 old-count))
        response-body (cljs/clj->js {:name name :visit_count new-count})]
    (do
      (swap! times-greeted assoc name new-count)
      (.send response response-body)
      (next))))

(defn create-server []
  (let [restify (node/require "restify")
        server (.createServer restify)
        static-file-regexp (js/RegExp. "^/\\?.*")
        static-server-opts (cljs/clj->js {:directory "./resources" :default "index.html"})
        static-file-server (.serveStatic restify static-server-opts)]
    (do
      (.get server "/greeting/:name" say-hello)
      (.get server static-file-regexp static-file-server))
    server))

(defn -main [& args]
  (let [web-server (create-server)]
    (.listen web-server 3000)) (println "localhost listening to port 3000 - localhost/greetings/:name"))

(enable-console-print!)

(set! *main-cli-fn* -main)
