(ns dynamodb.dynamodb-client
  (:require [cljs.nodejs :as node]
            [cljs.core :as cljs]))

(def express (node/require "express"))
(def app (express))
(def aws (node/require "aws-sdk"))
(def product-table {:TableName  "commerce.business.promote.PRODUCT"})
(def product-sku-search {:TableName  "commerce.business.promote.PRODUCT"
                           :KeyConditions {
                               :id {:ComparisonOperator "EQ",
                               :AttributeValueList [{:S "78CE7EB3D8AD4468940EE679D7D37307::BG-BRAND-4-2-3"}]
                               },
                               :sku {:ComparisonOperator "EQ",
                               :AttributeValueList [{:S "SKU_KEY"}]
                               }
                             }
                        })

(def product-all {:TableName  "commerce.business.promote.PRODUCT"
                           :KeyConditions {
                               :id {:ComparisonOperator "EQ",
                               :AttributeValueList [{:S "78CE7EB3D8AD4468940EE679D7D37307::BG-BRAND-4-2-3"}]
                               }
                             }
                        })


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

;;product sku search
(defn productSkuSearch [req res]
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
      (let [sku (aget req "params" "sku")]
        (let [new-product-sku-search (assoc-in product-sku-search[:KeyConditions :sku :AttributeValueList 0 :S]  sku)]
          (let [request (.query db (clj->js new-product-sku-search))]
            (.on request "complete" (fn [response]
                                      (if (.-error response)(.send res "error " (.-error response))
                                        (.send res "response " (.-data response)))))
            (.send request)))))))

;;all products search print on web browser
(defn productAll [req res]
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
          (let [request (.query db (clj->js product-all))]
            (.on request "complete" (fn [response]
                                      (if (.-error response)(.send res "error " (.-error response))
                                        (.send res "response " (.-data response)))))
            (.send request)))))


;;start of handler to write to red black tree
(defn handleProducts [err data]
  (let [test (-> data .-Items)
        lim (alength test)]
    (loop [i 0]
      (if (< i lim)
        (println "chris " (.-sku (aget test i))))
       (recur (inc i)))))


;;all products search use handler
(defn productAll2 [req res]
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
          (.query db (clj->js product-all) handleProducts)))
  (.send res "populating RB Tree with deals"))


(.get app "/tables" tables)
(.get app "/describeProductTable" describeProductTable)
(.get app "/search/sku/:sku" productSkuSearch)
(.get app "/search/allProducts" productAll)
(.get app "/search/allProducts2" productAll2)

(.listen app 8080)

(defn -main[& args]
  (println "Server started on port 8080"))

(enable-console-print!)
(set! *main-cli-fn* -main)
