(ns bank-api.create
  (:require [bank-api.memory :as memory]
            bank-api.spec
            [bank-api.utils :as utils]
            [clojure.spec.alpha :as s]
            [ring.util.response :as ring-response]))

(defn create-account [{:keys [params]}]
  (let [{:keys [name]} params
        account {:name           name
                 :balance        0
                 :audit-log      []
                 :account-number (memory/get-free-account-number)}]
    (if-not (s/valid? :bank-api.spec/account account)
      (ring-response/bad-request "There was a problem creating your account.")
      (do (memory/register-new-account! account)
          (utils/customer-safe-response account)))))
