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

(def bogoDeals (atom {}))

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

;;BEGINNING OF DEALS PROCESSING

(defn handleQualifiers [components]
  (loop [myComponents components]
    (if (not-empty myComponents)
      (let [qualifiers (aget (first myComponents) "qualifiers")]
        ;(println " QUALIFIERS " qualifiers "\n")
        (loop [myQualifiers qualifiers]
          (if (seq myQualifiers)
            (let [qualifier (first myQualifiers)]
              ;(println " QUALIFIER " qualifier "\n")
              ;(println " QUALIFIER_DEF.JAVA_TYPE " (.. qualifier -qualifierDef -javaType) "\n")
              (let [javaType (.. qualifier -qualifierDef -javaType)]
                (if (= "ProductQualifier" javaType)
                  ;(println " JSON_CONTENT "(.. qualifier -qualifierDef -jsonContent))))
                  (let [jsonContent (.. qualifier -qualifierDef -jsonContent)]
                    ;(println " JSON " (js/JSON.parse jsonContent))
                    (let [json (js/JSON.parse jsonContent)
                          skus (aget json "skus")
                          productCodes (aget json "productCodes")]
                      (doseq [sku skus]
                        ;(println " SKU " sku)
                        (swap! bogoDeals assoc sku sku)
                        (println " RUNNING COUNT OF DEALS MAP " (count @bogoDeals))
                        )

                      (doseq [productCode productCodes]
                        ;(println " PRODUCT CODE " productCode)
                        (swap! bogoDeals assoc productCode productCode)
                        (println " RUNNING COUNT OF DEALS MAP " (count @bogoDeals))
                        )

                    ))))
              (recur (rest myQualifiers)))))
        (recur (rest myComponents))))))

(defn handleComponents [deals]
    (loop [myDeals deals]
          (if (seq myDeals)
            (let [deal (first myDeals)]
              (println " DEAL " deal "\n")
              ;(println " COMPONENTS " (js/JSON.parse (get (get deal "components") "S")))))
              (let [components (js/JSON.parse (get (get deal "components") "S"))]
                ;(println " COMPONENTS " components "\n")
                (handleQualifiers components);; WE ARE GOING TO NEED TO INLINE THIS OR PASS THE DEAL SO THAT WE CAN PUT IT IN THE MAP
                )

              (println "END OF INDIVIDUAL COMPONENTS PROCESSING!!!! \n")
          (recur (rest myDeals)))))
          (println "END OF COMPONENTS AND DEALS PROCESSING!!!! \n")
          (println "HOW BIG IS THE BOGODEALS MAP " (count @bogoDeals)))


(defn handleDeals [err data]
  (let [test (-> data .-Items)
        test2 (seq (js->clj test))
        bogoDeals (filter (fn [x] (= "\"BOGO\"" (get (get x "deal-type") "S"))) test2)
        limBogoDeals (count bogoDeals)]
        (println "count " limBogoDeals)
        (handleComponents bogoDeals)))

(defn dealsAll [req res]
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
          (.query db (clj->js deals-all) handleDeals)))
  (.send res "populating with deals"))

;;END OF DEALS PROCESSING

(defn productAll2 [req res]
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
          (.query db (clj->js product-all) handleProductsMap)))
  (.send res "populating products"))


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
