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
        service-name "routeguide.RouteGuide"
        method-name "GetFeature"
        server (server/server
                {::server/name unique-server-name
                 ::server/services
                 [{::service/name service-name
                   ::service/methods
                   {method-name
                    (server/unary-rpc (partial get-feature features))}}]})
        started (server/start server)
        channel (client/in-process-channel unique-server-name)
        point {:latitude 1 :longitude 1}
        client (client/unary-rpc channel service-name method-name)
        feature {:name "name" :location point}]
      (try
        (is (nil? (client point)))
        (swap! features conj feature)
        (is (= feature (client point)))
        (finally
          (server/stop started)))))