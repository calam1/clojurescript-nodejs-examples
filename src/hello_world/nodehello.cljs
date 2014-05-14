(ns hello-world.nodehello
(:require [cljs.nodejs :as node]))

(def restify (node/require "restify"))

(defn respond [request response next]
  (let [name (aget request "params" "name")]
  (.send response (apply str "hello " name))))

(def server (.createServer restify))

(do
  (.get server "/hello/:name" respond)
  (.head server "/hello/:name" respond))

(defn -main [& args]
  (.listen server 8080 (println " localhost listening port 8080" )))

(enable-console-print!)
(set! *main-cli-fn* -main)