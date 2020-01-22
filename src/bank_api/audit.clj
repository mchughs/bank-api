(ns bank-api.audit
  (:require [bank-api.memory :as memory]
            [bank-api.utils :as utils]
            [clojure.data.json :as json]))

(defn get-log [{:keys [route-params]}]
  (let [{:keys [id]} route-params]
    (if-let [account (memory/get-account id)]
      (-> account :audit-log reverse json/write-str)
      (utils/missing-account-response id))))
