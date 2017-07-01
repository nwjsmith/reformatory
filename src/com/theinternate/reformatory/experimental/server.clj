(ns com.theinternate.reformatory.experimental.server
  (:require [com.theinternate.reformatory.experimental.service :as service])
  (:import (io.grpc Server
                    ServerBuilder
                    ServerServiceDefinition
                    ServerServiceDefinition$Builder)
           (io.grpc.inprocess InProcessServerBuilder)
           (io.grpc.stub ServerCalls
                         ServerCalls$UnaryMethod)))

(defn unary-rpc
  [f]
  (reify ServerCalls$UnaryMethod
    (invoke [_ request responseObserver]
      (let [response (f request)]
        (.onNext responseObserver (if (nil? response) :sentinel/nil response))
        (.onCompleted responseObserver)
        responseObserver))))

(defn- service-definition
  ^ServerServiceDefinition [service]
  (let [name (::service/name service)]
    (.build ^ServerServiceDefinition$Builder
            (reduce (fn [^ServerServiceDefinition$Builder builder [method rpc]]
                      (.addMethod builder
                                  (service/method-descriptor name method)
                                  (ServerCalls/asyncUnaryCall rpc)))
                    (ServerServiceDefinition/builder ^String name)
                    (::service/methods service)))))

(defn server
  [configuration]
  (.build ^ServerBuilder
          (reduce (fn [^ServerBuilder builder service]
                    (.addService builder (service-definition service)))
                  (InProcessServerBuilder/forName (::name configuration))
                  (::services configuration))))

(defn start
  [^Server server]
  (.start server))

(defn stop [^Server server]
  (.shutdown server))