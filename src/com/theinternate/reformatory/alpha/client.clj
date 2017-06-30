(ns com.theinternate.reformatory.alpha.client
  (:require [cognitect.transit :as transit])
  (:import (io.grpc CallOptions
                    MethodDescriptor
                    MethodDescriptor$Marshaller
                    MethodDescriptor$MethodType)
           (io.grpc.stub ClientCalls)
           (io.grpc.inprocess InProcessChannelBuilder)
           (java.io PipedInputStream
                    PipedOutputStream)))

(def ^:private transit-marshaller
  (reify MethodDescriptor$Marshaller
    (parse [_ stream]
      (-> stream (transit/reader :json) transit/read))
    (stream [_ value]
      (let [in (PipedInputStream. 4096)]
        (with-open [out (PipedOutputStream. in)]
          (-> out (transit/writer :json) (transit/write value)))
        in))))

(defn unary-rpc
  [channel service-name method-name]
  (fn [request]
    (let [method (MethodDescriptor/create MethodDescriptor$MethodType/UNARY
                                          (MethodDescriptor/generateFullMethodName service-name method-name)
                                          transit-marshaller
                                          transit-marshaller)]
      (let [response (ClientCalls/blockingUnaryCall channel
                                                    method
                                                    CallOptions/DEFAULT
                                                    request)]
        (if (= :sentinel/nil response)
          nil
          response)))))

(defn in-process-channel [server-name]
  (.build (.directExecutor (InProcessChannelBuilder/forName server-name))))