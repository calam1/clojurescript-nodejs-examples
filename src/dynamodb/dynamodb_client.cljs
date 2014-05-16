(ns dynamodb.dynamodb-client
  (:require [cljs.nodejs :as node]
            [cljs.core :as cljs]))

(def express (node/require "express"))
(def app (express))
(def aws (node/require "aws-sdk"))

(defn handle-it [err data]
  (println "in the handler " data "error value " err)
  )

;;this function just uses a callback to print and we can't send a response back or at least I don't know how to :)
;(defn tables [req res]
;  (let [config (.-config aws)]
;    (set! (.-region config) "us-east-1")
;    (let [db (aws.DynamoDB.)]
;      (.listTables db handle-it))))

;;this function is similar to the one above except we do not specify a callback for AWS we manually send
;;need to handle failure, etc - look at amazon documentation for aws-sdk for nodejs
(defn tables [req res]
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
      (let [request (.listTables db)]
        (.on request "success" (fn [response] (.send res "response " (.-data response))))
      (.send request)))))


(.get app "/tables" tables)
(.get app "/search/productId/:productId" productSearch)

(.listen app 8080)

(defn -main[& args]
  (println "Server started on port 8080"))

(enable-console-print!)
(set! *main-cli-fn* -main)
