(ns com.theinternate.reformatory.experimental.client
  (:require [com.theinternate.reformatory.experimental.service :as service])
  (:import (io.grpc CallOptions
                    Channel)
           (io.grpc.stub ClientCalls)
           (io.grpc.inprocess InProcessChannelBuilder)))

(defn unary-rpc
  [channel service-name method-name]
  (fn [request]
    (ClientCalls/blockingUnaryCall channel
                                   (service/method-descriptor service-name
                                                              method-name)
                                   CallOptions/DEFAULT
                                   request)))

(defn server-streaming-rpc [channel service-name method-name]
  (fn [request responseObserver]
    (ClientCalls/asyncServerStreamingCall (.newCall ^Channel channel
                                                    (service/method-descriptor service-name
                                                                               method-name)
                                                    CallOptions/DEFAULT)
                                          request
                                          responseObserver)))

(defn in-process-channel [server-name]
  (.build (.directExecutor (InProcessChannelBuilder/forName server-name))))