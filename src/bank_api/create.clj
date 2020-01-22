(ns bank-api.create
  (:require [bank-api.memory :as memory]
            bank-api.spec
            [bank-api.utils :as utils]
            [clojure.spec.alpha :as s]))

(defn create-account [{:keys [params]}]
  (let [{:keys [name]} params
        account {:name           name
                 :balance        0
                 :audit-log      []
                 :account-number @memory/free-account-number}]
    (if-not (s/valid? :bank-api.spec/account account)
      (utils/bad-request "There was a problem creating your account.")
      (do (memory/register-new-account! account)
          (utils/customer-safe-response account)))))
