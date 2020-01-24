(defproject bank-api "0.1.0-SNAPSHOT"
  :description "Bank-API project for LemonPi."
  :min-lein-version "2.0.0"
  :source-paths ["src" "resources"]
  :test-paths ["test/bank_api"]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "0.2.7"]
                 ;;Routing
                 [compojure "1.6.1"]
                 ;;Server
                 [ring/ring-core "1.8.0"]
                 [ring/ring-defaults "0.3.2"]
                 [aleph "0.4.6"]
                 ;;Configuration
                 [aero "1.1.5"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler bank-api.handler/app}
  :profiles
  {:test {:dependencies [[javax.servlet/servlet-api "2.5"]
                         [ring/ring-mock "0.3.2"]
                         ;;Load testing
                         [clj-gatling "0.14.0"]]}})
