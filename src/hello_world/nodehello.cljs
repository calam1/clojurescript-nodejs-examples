(ns hello-world.nodehello
(:require [cljs.nodejs :as node]))

(def express (node/require "express"))

(def app (express))

(defn -main [& args]
  (. app (get "/hello/:name" (fn [req res]
                    (let [name (aget req "params" "name")]
                    (. res (send (apply str "express server hello world " name)))))))
  (.log js/console (str "Express server started on port: " (.-port (.address (.listen app 8080))))))

(enable-console-print!)

(set! *main-cli-fn* -main)
