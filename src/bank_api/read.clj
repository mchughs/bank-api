(ns bank-api.read
  (:require [bank-api.memory :as memory]
            [bank-api.utils :as utils]))

(defn read-account [{:keys [route-params]}]
  (let [{:keys [id]} route-params]
    (if-let [account (memory/get-account id)]
      (utils/customer-safe-response account)
      (utils/missing-account-response id))))
