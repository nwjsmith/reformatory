(ns com.theinternate.reformatory.alpha.server
  (:require [cognitect.transit :as transit])
  (:import (io.grpc BindableService
                    MethodDescriptor
                    MethodDescriptor$Marshaller
                    MethodDescriptor$MethodType
                    Server
                    ServerServiceDefinition)
           (io.grpc.inprocess InProcessServerBuilder)
           (io.grpc.stub ServerCalls
                         ServerCalls$UnaryMethod)
           (java.io PipedInputStream
                    PipedOutputStream)))

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

(def ^:private transit-marshaller
  (reify MethodDescriptor$Marshaller
    (parse [_ stream]
      (-> stream (transit/reader :json) transit/read))
    (stream [_ value]
      (let [in (PipedInputStream. 4096)]
        (with-open [out (PipedOutputStream. in)]
          (-> out (transit/writer :json) (transit/write value)))
        in))))

(defn server
  [configuration]
  (let [builder (InProcessServerBuilder/forName (::name configuration))]
    (.addService builder
                 (reify BindableService
                   (bindService [_]
                     (let [service-name "routeguide.RouteGuide"]
                       (-> (ServerServiceDefinition/builder service-name)
                           (.addMethod
                            (MethodDescriptor/create MethodDescriptor$MethodType/UNARY
                                                     (MethodDescriptor/generateFullMethodName service-name "GetFeature")
                                                     transit-marshaller
                                                     transit-marshaller)
                            (ServerCalls/asyncUnaryCall (get-in configuration [::services 0 ::service-methods "GetFeature"])))
                           .build)))))
    (.build builder)))

(defn start
  [^Server server]
  (.start server))

(defn stop [^Server server]
  (.shutdown server))
