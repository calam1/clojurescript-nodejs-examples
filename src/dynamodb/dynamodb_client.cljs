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
                               :AttributeValueList [{:S "78CE7EB3D8AD4468940EE679D7D37307"}]
                               }
                             }
                        })

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
                        (swap! bogoDeals assoc sku deal)
                        )
                      (doseq [productCode productCodes]
                        (swap! bogoDeals assoc productCode deal)
                        )
                    ))))
              (recur (rest myQualifiers)))))
        (recur (rest myComponents))))))

(defn handleComponents [deals]
    (loop [myDeals deals]
          (if (seq myDeals)
            (let [deal (first myDeals)]
              (let [components (js/JSON.parse (get (get deal "components") "S"))]
                (handleQualifiers components deal))
          (recur (rest myDeals)))))
          (println "TEST GET A DEAL FOR PRODUCT CODE 3m-168 " (@bogoDeals "3m-168"))
          (println "TEST GET A DEAL FOR PRODUCT CODE 3l-168 " (@bogoDeals "3l-168")))

(defn handleDeals [err data]
  (let [items (-> data .-Items)
        cljItems (seq (js->clj items))
        bogoDeals (filter (fn [x] (= "\"BOGO\"" (get (get x "deal-type") "S"))) cljItems)
        limBogoDeals (count bogoDeals)]
        (println "count " limBogoDeals)
        (handleComponents bogoDeals)))

(defn dealsAll [req res]
  (let [config (.-config aws)]
    (set! (.-region config) "us-east-1")
    (let [db (aws.DynamoDB.)]
          (.query db (clj->js deals-all) handleDeals)))
  (.send res "populating with deals"))

(defn evaluate [req res]
  (let [body (aget req "body")]
    ;(println " BODY OF REQUEST " (js->clj body :keywordize-keys true))
    (let [{:keys [retailTransactionId date localCurrency retailChannel subTotal lines]} (js->clj body :keywordize-keys true)]
      (doseq [line lines]
        ;(println " LINE " line)
        (let [{lineSeq :lineSeq {productCode :productCode sku :sku} :item quantity :quantity unitPrice :unitPrice extendedPrice :extendedPrice} line]
          ;(println " LINE DESTRUCTURED lineseq: " lineSeq " productCode: " productCode " sku: " sku " qty:" quantity " unit px: " unitPrice " extend px " extendedPrice)
        )
      (.send res (str "lines: " lines))))))

(.get app "/search/allDeals" dealsAll)
(.post app "/evaluate" evaluate)

(.listen app 8080)

(defn -main[& args]
  (println "Server started on port 8080"))

(enable-console-print!)
(set! *main-cli-fn* -main)
