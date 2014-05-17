(ns dynamodb.dynamodb-client
  (:require [cljs.nodejs :as node]
            [cljs.core :as cljs]))

(def express (node/require "express"))
(def app (express))
(def aws (node/require "aws-sdk"))
(def product-table {:TableName  "commerce.business.promote.PRODUCT"})

(defn handle-it [err data]
  (println "in the handler " data "error value " err))

;;this function just uses a callback to print and we can't send a response back or at least I don't know how to :)
;(defn tables [req res]
;  (let [config (.-config aws)]
;    (set! (.-region config) "us-east-1")
;    (let [db (aws.DynamoDB.)]
;      (.listTables db handle-it))))

;;this function is similar to the one above except we do not specify a callback for AWS we manually send
(defn tables [req res]
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
      (let [request (.listTables db)]
        (.on request "complete" (fn [response]
                                  (if (.-error response)(.send res "error " (.-error response))
                                      (.send res "response " (.-data response)))))
        (.send request)))))

;;describe product table
(defn describeProductTable [req res]
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
      (let [request (.describeTable db (clj->js product-table))];;clj->js converts map to json
        (.on request "complete" (fn [response]
                                  (if (.-error response)(.send res "error " (.-error response))
                                     (.send res "response " (.-data response)))))
      (.send request)))))


(.get app "/tables" tables)
(.get app "/describeProductTable" describeProductTable)
;(.get app "/search/sku/:sku" productSkuSearch)

(.listen app 8080)

(defn -main[& args]
  (println "Server started on port 8080"))

(enable-console-print!)
(set! *main-cli-fn* -main)
