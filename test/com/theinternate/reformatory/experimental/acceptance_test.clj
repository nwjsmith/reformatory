(ns com.theinternate.reformatory.experimental.acceptance-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.theinternate.reformatory.experimental.client :as client]
            [com.theinternate.reformatory.experimental.server :as server]
            [com.theinternate.reformatory.experimental.service :as service]))

(defn- get-feature
  [features location]
  (first (filter #(= location (:location %)) @features)))

(deftest get-feature-test
  (let [unique-server-name (str "in-process server for " (ns-name *ns*))
        features (atom [])
        server (server/server {::server/name unique-server-name
                               ::server/services
                               [{::service/name "routeguide.RouteGuide"
                                 ::service/methods
                                 {"GetFeature"
                                  (server/unary-rpc (partial get-feature features))}}]})
        started (server/start server)
        channel (client/in-process-channel unique-server-name)
        point {:latitude 1 :longitude 1}]
      (try
          (is (nil? ((client/unary-rpc channel "routeguide.RouteGuide" "GetFeature") point)))
          (swap! features conj {:name "name" :location point})
          (is (= {:name "name" :location point}
                 ((client/unary-rpc channel "routeguide.RouteGuide" "GetFeature") point)))
          (finally
              (server/stop started)))))