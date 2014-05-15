(ns hello-world.nodehello
(:require [cljs.nodejs :as node]
          [cljs.core :as cljs]))

(def express (js/require "express"))
(def app (express))
(def fs (js/require "fs"))
(def *rs* nil)

(.get app "/" (fn [req res]
                (.send res "Hello world!")))

(.get app "/user/:name" (fn [req res]
                          (.send res (aget req "params" "name"))))


(.get app "/read"
      (fn [req res]
        (set! *rs* (.createReadStream fs "/Users/clam/test.txt"))
        (.pipe *rs* res)))

(.listen app 3000)

(defn -main[& _]
  (println "Server started on port 3000"))

(enable-console-print!)

(set! *main-cli-fn* -main)
