(defproject com.theinternate/reformatory "0.1.0"
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
  :deploy-repositories
  {"releases"
   {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    :username :env/sonatype_username
    :password :env/sonatype_password}}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]]
  :pedantic? :abort
  :global-vars {*warn-on-reflection* true}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [io.grpc/grpc-core "1.4.0"]
                 [io.grpc/grpc-stub "1.4.0"]
                 [com.cognitect/transit-clj "0.8.300"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.trace "0.7.9"]]}})