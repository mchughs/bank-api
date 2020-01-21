(defproject bank-api "0.1.0-SNAPSHOT"
  :description "Bank-API project for LemonPi."
  :min-lein-version "2.0.0"
  :dependencies [[compojure "1.6.1"]
                 [hikari-cp "2.10.0"]
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "0.2.7"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "42.2.9.jre7"]
                 [ring/ring-defaults "0.3.2"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler bank-api.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
