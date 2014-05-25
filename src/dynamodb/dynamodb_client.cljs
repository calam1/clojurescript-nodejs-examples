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

(def deals-all {:TableName  "commerce.business.promote.DEAL"
                           :KeyConditions {
                               :id {:ComparisonOperator "EQ",
                               :AttributeValueList [{:S "78CE7EB3D8AD4468940EE679D7D37307"}]
                               }
                             }
                        })

(def deals (atom {}))

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

;;handler for products using loop and recur print in logs
(defn handleProductsLoop [err data]
  (let [test (-> data .-Items)
        lim (alength test)]
    (loop [i 0]
      (if (< i lim)
        (println "counter " i " " (.-S (.-sku (aget test i)))))
       (recur (inc i)))))

;;handler for products using seq and adding to map - incomplete - experimental
(defn handleProductsMap [err data]
  (let [test (-> data .-Items)
       testMap (map #(hash-map % "testValue")(seq (js->clj test)))]
    ))

;;handler for deals using loop and recur
(defn handleDealsLoop [err data]
  (let [test (-> data .-Items)
        lim (alength test)]
    (loop [i 0]
      (if (< i lim)
        (let [test2 (aget test i)]
          (let [test3  (.-S (aget test2 "deal-type"))]
            (println i " test " test3))
       (recur (inc i)))))))

;;handler for deals using seq and filter all BOGO deal types - next step is adding to map
(defn handleDeals [err data]
  (let [test (-> data .-Items)
        test2 (seq (js->clj test))
        test3 (filter (fn [x] (= "\"BOGO\"" (get (get x "deal-type") "S"))) test2)
        test4 (get (first test3) "components");;need to loop recur
        test5 (get test4 "S")
        test6 (js/JSON.parse test5);;have to do this because qualifiers are stored as a string in the components column
        test7 (first test6);;need loop recur
        test8 (aget test7 "qualifiers")
        test9 (second test8);;need loop recur
        testa (aget test9 "qualifierDef")
        testb (aget testa "javaType")
        testc (aget testa "jsonContent")
        testd (js/JSON.parse testc);;skus were stored as string, as it is labeled json content
        teste (aget testd "skus")
        testf (first teste);;need loop recur
        ]
    (println testf)
    ;deals (map #(hash-map % "testValue") test3)]
    ;(println deals)
    (println  (count test3))))

(defn productAll2 [req res]
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
          (.query db (clj->js product-all) handleProductsMap)))
  (.send res "populating products"))

(defn dealsAll [req res]
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
          (.query db (clj->js deals-all) handleDeals)))
  (.send res "populating with deals"))

(.get app "/tables" tables)
(.get app "/describeProductTable" describeProductTable)
(.get app "/search/sku/:sku" productSkuSearch)
(.get app "/search/allProducts" productAll)
(.get app "/search/allProducts2" productAll2)
(.get app "/search/allDeals" dealsAll)

(.listen app 8080)

(defn -main[& args]
  (println "Server started on port 8080"))

(enable-console-print!)
(set! *main-cli-fn* -main)
