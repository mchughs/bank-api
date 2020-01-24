(ns bank-api.send
  (:require [bank-api.memory :as memory]
            [bank-api.transact :as transact]
            [bank-api.utils :as utils]
            [ring.util.response :as ring-response]))

(defn- invalid-ids? [sender-id receiver-id]
  (let [sender   (memory/get-account sender-id)
        receiver (memory/get-account receiver-id)]
    (cond
      (= sender-id receiver-id) (ring-response/bad-request "Can't send money to own account.")
      (nil? sender)             (utils/missing-account-response sender-id)
      (nil? receiver)           (utils/missing-account-response receiver-id))))

(defn- process-deposit
  "A completely successful transfer from A->B will return the json account info on the sender A."
  [{:keys [id amount account-number]} sender-res]
  (let [deposit-req {:route-params {:id account-number}
                     :params {:amount amount}}
        deposit-res (transact/deposit deposit-req {:transfer-account id})
        receiver-err? (= (:status deposit-res) 400)]
    (if receiver-err?
      deposit-res
      sender-res)))

(defn- process-withdraw [{:keys [id amount account-number] :as send-data}]
  (let [withdraw-req {:route-params {:id id}
                      :params {:amount amount}}
        withdraw-res (transact/withdraw withdraw-req {:transfer-account account-number})
        sender-err? (= (:status withdraw-res) 400)]
    (if sender-err?
      withdraw-res
      (process-deposit send-data withdraw-res))))

(defn send-money [{:keys [route-params params]}] ;;TODO the sender will still lose money if their transaction fails. Expand test to catch this
  (let [{:keys [id]} route-params
        {:keys [account-number amount]} params
        send-data {:id id
                   :amount amount
                   :account-number account-number}]
    (if-let [invalid-ids (invalid-ids? id account-number)]
      invalid-ids
      (process-withdraw send-data))))
