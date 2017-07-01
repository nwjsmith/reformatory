(ns com.theinternate.reformatory.experimental.acceptance-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.theinternate.reformatory.experimental.client :as client]
            [com.theinternate.reformatory.experimental.server :as server]
            [com.theinternate.reformatory.experimental.service :as service])
  (:import (io.grpc.stub StreamObserver)
           (java.util.concurrent CountDownLatch
                                 TimeUnit)))

(defn- get-feature
  [features location responseObserver]
  (.onNext responseObserver
           (or (first (filter #(= location (:location %)) @features))
               {:name "" :location location}))
  (.onCompleted responseObserver))

(defn- list-features
  [features rectangle responseObserver]
  (doseq [bounded (filter (fn [feature]
                            (and (not-empty (:name feature))
                                 (<= (:longitude (:lo rectangle))
                                     (:longitude (:location feature))
                                     (:longitude (:hi rectangle)))
                                 (<= (:latitude (:lo rectangle))
                                     (:latitude (:location feature))
                                     (:latitude (:hi rectangle)))))
                          @features)]
    (.onNext responseObserver bounded))
  (.onCompleted responseObserver))

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
        feature {:name "name" :location point}
        unnamed-feature {:name "" :location point}]
      (try
        (is (= unnamed-feature (client point)))
        (swap! features conj feature)
        (is (= feature (client point)))
        (finally
          (server/stop started)))))

(deftest list-features-test
  (let [unique-server-name (str "in-process server for " (ns-name *ns*))
        features (atom [])
        service-name "routeguide.RouteGuide"
        method-name "ListFeatures"
        server (server/server
                {::server/name unique-server-name
                 ::server/services
                 [{::service/name service-name
                   ::service/methods
                   {method-name
                    (server/server-streaming-rpc (partial list-features features))}}]})
        started (server/start server)
        channel (client/in-process-channel unique-server-name)
        rect {:lo {:latitude 0 :longitude 0} :hi {:latitude 10 :longitude 10}}
        f1 {:name "f1" :location {:longitude -1 :latitude -1}}
        f2 {:name "f2" :location {:longitude 2 :latitude 2}}
        f3 {:name "f3" :location {:longitude 3 :latitude 3}}
        f4 {:name "" :location {:longitude 4 :latitude 4}}
        list-features-client (client/server-streaming-rpc channel service-name method-name)
        result (atom #{})
        latch (CountDownLatch. 1)]
    (try
      (swap! features conj f1 f2 f3 f4)
      (list-features-client rect (reify StreamObserver
                                   (onNext [_ value] (swap! result conj value))
                                   (onError [_ throwable] (throw throwable))
                                   (onCompleted [_] (.countDown latch))))
      (is (.await latch 1 TimeUnit/SECONDS))
      (is (= #{f2 f3} @result))
      (finally
        (server/stop started)))))