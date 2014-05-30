(ns dynamodb.dynamodb-client
  (:require [cljs.nodejs :as node]
            [cljs.core :as cljs]))

(def express (node/require "express"))
(def express-middlewares (node/require "express-middlewares"))
(def app (express))
(.use app (.bodyParser express-middlewares))
(def aws (node/require "aws-sdk"))
(def deals-all {:TableName  "commerce.business.promote.DEAL"
                           :KeyConditions {
                               :id {:ComparisonOperator "EQ",
                               :AttributeValueList [{:S "78CE7EB3D8AD4468940EE679D7D37307"}]}}})

(def port (or process.env.PORT 3000))
(def bogoDeals (atom {}))

(defn handleQualifiers [components deal]
  (loop [myComponents components]
    (if (not-empty myComponents)
      (let [qualifiers (aget (first myComponents) "qualifiers")]
        (loop [myQualifiers qualifiers]
          (if (seq myQualifiers)
            (let [qualifier (first myQualifiers)]
              (let [javaType (.. qualifier -qualifierDef -javaType)]
                (if (= "ProductQualifier" javaType)
                  (let [jsonContent (.. qualifier -qualifierDef -jsonContent)]
                    (let [json (js/JSON.parse jsonContent)
                          skus (aget json "skus")
                          productCodes (aget json "productCodes")]
                      (doseq [sku skus]
                        (swap! bogoDeals assoc sku deal))
                      (doseq [productCode productCodes]
                        (swap! bogoDeals assoc productCode deal)
                        )))))
              (recur (rest myQualifiers)))))
        (recur (rest myComponents))))))

(defn handleComponents [deals]
    (loop [myDeals deals]
          (if (seq myDeals)
            (let [deal (first myDeals)]
              (let [components (js/JSON.parse (get (get deal "components") "S"))]
                (handleQualifiers components deal))
          (recur (rest myDeals))))))

(defn handleDeals [err data]
  (let [items (-> data .-Items)
        cljItems (seq (js->clj items))
        bogoDeals (filter (fn [x] (= "\"BOGO\"" (get (get x "deal-type") "S"))) cljItems)
        limBogoDeals (count bogoDeals)]
        (println "count " limBogoDeals)
        (handleComponents bogoDeals)))

(defn evaluate [req res]
  (let [body (aget req "body")]
    (let [{:keys [retailTransactionId date localCurrency retailChannel subTotal lines]} (js->clj body :keywordize-keys true)]
      (doseq [line lines]
        (let [{lineSeq :lineSeq {productCode :productCode sku :sku} :item quantity :quantity unitPrice :unitPrice extendedPrice :extendedPrice} line]
          (let [deal (@bogoDeals productCode)]
            (let [components (js/JSON.parse (get (get deal "components") "S"))]
              (doseq [myComponents components]
                (let [qualifiers (aget myComponents "qualifiers")
                      benefit (aget myComponents "benefit")]
                  (doseq [myQualifiers qualifiers]
                    (let [javaType (.. myQualifiers -qualifierDef -javaType)]
                      (if (= "ProductQualifier" javaType)
                        (let [jsonContent (.. myQualifiers -qualifierDef -jsonContent)]
                          (let [json (js/JSON.parse jsonContent)
                                productCodes (aget json "productCodes")]
                            (doseq [prodCode productCodes]
                              (if (= productCode prodCode)
                                (if (not (nil? benefit))
                                  (if (= 2 quantity);;oversimplified unrealistic evaluation
                                     (let [returnJson line];;just return the line getting the deal
                                  (.send res (clj->js returnJson)))))))))))))))))))))

(defn dealsAll [req res]
  (if (not-empty @bogoDeals)
    (evaluate req res)
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
      (let [request (.query db (clj->js deals-all))]
        (.on request "complete" (fn [response] (handleDeals (.-error response) (.-data response)) (evaluate req res)))
        (.send request))))))

(.post app "/evaluate" dealsAll)

(.listen app port)

(defn -main[& args]
  (println "Server started on port: " port))

(enable-console-print!)
(set! *main-cli-fn* -main)
