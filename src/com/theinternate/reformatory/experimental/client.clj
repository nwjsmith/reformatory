(ns com.theinternate.reformatory.experimental.client
  (:require [com.theinternate.reformatory.experimental.service :as service])
  (:import (io.grpc CallOptions)
           (io.grpc.stub ClientCalls)
           (io.grpc.inprocess InProcessChannelBuilder)))

(defn unary-rpc
  [channel service-name method-name]
  (fn [request]
    (let [response (ClientCalls/blockingUnaryCall channel
                                                  (service/method-descriptor service-name method-name)
                                                  CallOptions/DEFAULT
                                                  request)]
      (if (= :sentinel/nil response)
        nil
        response))))

(defn in-process-channel [server-name]
  (.build (.directExecutor (InProcessChannelBuilder/forName server-name))))