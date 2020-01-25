(ns bank-api.transact
  (:require [bank-api.memory :as memory]
            bank-api.spec
            [bank-api.utils :as utils]
            [clojure.spec.alpha :as s]
            [ring.util.response :as ring-response]))

(defn- deposit-amount-invalid [amount]
  (ring-response/bad-request (format "The amount (%d) cannot be deposited into an account." amount)))

(defn- deposit-success [{:keys [account amount id transfer-account]}]
  (let [balance          (+ (:balance account)
                            amount)
        log              {:id id
                          :credit amount
                          :description (if transfer-account (str "received from #" transfer-account) "deposit")}
        new-account-data (-> account
                             (assoc :balance balance)
                             (utils/append-log log))]
    (memory/mutate-account! id new-account-data)
    (utils/customer-safe-response new-account-data)))

;---------------------------------------------------------------------------------------------------------------

(defn- withdraw-amount-invalid [amount]
  (ring-response/bad-request (format "The amount (%d) cannot be withdrawn from an account." amount)))

(defn- withdraw-balance-invalid [balance]
  (ring-response/bad-request (format "The balance of an account cannot go as low as (%d)." balance)))

(defn- withdraw-success [balance {:keys [account amount id transfer-account]}]
  (let [log              {:id id
                          :debit amount
                          :description (if transfer-account (str "sent to #" transfer-account) "withdraw")}
        new-account-data (-> account
                             (assoc :balance balance)
                             (utils/append-log log))]
  (memory/mutate-account! id new-account-data)
  (utils/customer-safe-response new-account-data)))


(defn- withdraw-check-balance [{:keys [account amount] :as transaction-data}]
  (let [balance (- (:balance account) amount)]
    (if-not (s/valid? :bank-api.spec/balance balance)
      (withdraw-balance-invalid balance)
      (withdraw-success balance transaction-data))))

;----------------------------------------------------------------------------------------------------------

(defn- transact
  [{:keys [fail-res-f process-f]} {:keys [route-params params]} & [{:keys [transfer-account]}]]
  (let [{:keys [id]} route-params
        {:keys [amount]} params
        amount (Integer. amount)]
    (if-not (s/valid? :bank-api.spec/transaction-amount amount)
      (fail-res-f amount)
      (if-let [account (memory/get-account id)] ;; TODO Theere should be no space between inspecting the current account and writing the new account.
        (process-f {:account account :amount amount :id id :transfer-account transfer-account})
        (utils/missing-account-response id)))))

(def deposit
  (partial transact {:fail-res-f deposit-amount-invalid
                     :process-f deposit-success}))

(def withdraw
  (partial transact {:fail-res-f withdraw-amount-invalid
                     :process-f withdraw-check-balance}))
