(ns com.theinternate.reformatory.alpha.server
  (:require [com.theinternate.reformatory.alpha.service :as service])
  (:import (io.grpc BindableService
                    Server
                    ServerServiceDefinition)
           (io.grpc.inprocess InProcessServerBuilder)
           (io.grpc.stub ServerCalls
                         ServerCalls$UnaryMethod)))

(defn unary-rpc
  [f]
  (reify ServerCalls$UnaryMethod
    (invoke [_ request responseObserver]
      (let [response (f request)]
        (if (nil? response)
          (.onNext responseObserver :sentinel/nil)
          (.onNext responseObserver response))
        (.onCompleted responseObserver)
        responseObserver))))

(defn server
  [configuration]
  (let [builder (InProcessServerBuilder/forName (::name configuration))]
    (.addService builder
                 (reify BindableService
                   (bindService [_]
                     (let [service-name "routeguide.RouteGuide"
                           method-name "GetFeature"]
                       (-> (ServerServiceDefinition/builder service-name)
                           (.addMethod
                            (service/method-descriptor service-name method-name)
                            (ServerCalls/asyncUnaryCall (get-in configuration [::services 0 ::service/methods method-name])))
                           .build)))))
    (.build builder)))

(defn start
  [^Server server]
  (.start server))

(defn stop [^Server server]
  (.shutdown server))