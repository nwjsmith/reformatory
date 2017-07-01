(defproject com.theinternate/reformatory "0.1.0-SNAPSHOT"
  :description "Coming soon."
  :url "https://github.com/nwjsmith/reformatory"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git"
        :tag "HEAD"
        :url "https://github.com/nwjsmith/reformatory"}
  :pom-addition [:developers [:developer {:id "nwjsmith"}
                              [:name "Nate Smith"]
                              [:url "http://theinternate.com"]]]
  :pedantic? :abort
  :global-vars {*warn-on-reflection* true}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [io.grpc/grpc-core "1.4.0"]
                 [io.grpc/grpc-stub "1.4.0"]
                 [com.cognitect/transit-clj "0.8.300"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.trace "0.7.9"]]}})