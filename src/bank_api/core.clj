(ns bank-api.core
  (:require [aero.core        :as aero]
            [aleph.http       :as aleph]
            [bank-api.handler :as handler]
            [clojure.java.io  :as io]))

(def config (aero/read-config (io/resource "config.edn")))
(def base-url (:base-url config))
(def port (:port config))

(defn start-server [] ;; Partially silenced with our resources/logback.xml
  (aleph/start-server handler/app {:port port}))

(defn stop-server [server]
  (when server
    (.close ^java.io.Closeable server)))
