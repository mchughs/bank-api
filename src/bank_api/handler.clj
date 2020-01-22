(ns bank-api.handler
  (:require [bank-api.memory :as memory]
            bank-api.spec
            [clojure.data.json :as json]
            [clojure.spec.alpha :as s]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

;;TODOs spec stuff, db

(defn hide-log [account]
  (dissoc account :audit-log))

(defn create-account [{:keys [params]}]
  (let [{:keys [name]} params
        new-account    {:name           name
                        :balance        0
                        :audit-log      []
                        :account-number @memory/free-account-number}]
    (memory/register-new-account! new-account)
    (json/write-str (hide-log new-account))))

(defn- read-account [{:keys [route-params]}]
  (let [{:keys [id]} route-params]
    (if-let [account (memory/get-account id)]
      (json/write-str (hide-log account))
      {:status 400 :body (format "No account associated with account-number %s." id)})))

(defn- deposit [{:keys [route-params params]} & [{:keys [transfer-account]}]]
  (let [{:keys [id]} route-params
        {:keys [amount]} params
        amount (Integer. amount)]
    (if-not (s/valid? :bank-api.spec/transaction-amount amount)
      {:status 400 :body (format "The amount (%d) cannot be deposited into an account." amount)}
      (if-let [account (memory/get-account id)]
        (let [new-balance (+ (:balance account)
                          amount)
              new-account-data (assoc account :balance new-balance)]
          (memory/mutate-account! id new-account-data)
          (memory/push-to-log! {:id id :credit amount :description (if transfer-account (str "received from #" transfer-account) "deposit")})
          (json/write-str (hide-log new-account-data)))
        {:status 400 :body (format "No account associated with account-number %s." id)}))))

(defn- withdraw [{:keys [route-params params]} & [{:keys [transfer-account]}]]
  (let [{:keys [id]} route-params
        {:keys [amount]} params
        amount (Integer. amount)]
    (if-not (s/valid? :bank-api.spec/transaction-amount amount)
      {:status 400 :body (format "The amount (%d) cannot be withdrawn from an account." amount)}
      (if-let [account (memory/get-account id)]
        (let [new-balance (- (:balance account)
                             amount)]
          (if-not (s/valid? :bank-api.spec/balance new-balance)
            {:status 400 :body (format "The balance of an account cannot go as low as (%d)." new-balance)}
            (let [new-account-data (assoc account :balance new-balance)]
              (memory/mutate-account! id new-account-data)
              (memory/push-to-log! {:id id :debit amount :description (if transfer-account (str "sent to #" transfer-account) "withdraw")})
              (json/write-str (hide-log new-account-data)))))
        {:status 400 :body (format "No account associated with account-number %s." id)}))))

(defn- send-money [{:keys [route-params params]}] ;;TODO the sender will still lose money if their transaction fails. Expand test to catch this
  (let [{:keys [id]} route-params
        {:keys [account-number amount]} params]
    (if (= id account-number)
      {:status 400 :body "Can't send money to own account."}
      (let [withdraw-req {:route-params {:id id}
                          :params {:amount amount}}
            withdraw-res (withdraw withdraw-req {:transfer-account account-number})
            sender-err? (= (:status withdraw-res) 400)]
        (if sender-err?
          withdraw-res
          (let [deposit-req {:route-params {:id account-number}
                             :params {:amount amount}}
                deposit-res (deposit deposit-req {:transfer-account id})
                receiver-err? (= (:status deposit-res) 400)]
            (if receiver-err?
              deposit-res
              withdraw-res)))))))

(defn get-log [{:keys [route-params]}]
  (let [{:keys [id]} route-params
        account (memory/get-account id)]
    (if account
      (-> account :audit-log reverse json/write-str)
      {:status 400 :body (format "No account associated with account-number %s." id)})))

(defroutes app-routes
  (POST "/account" [] create-account)
  (GET  "/account/:id" [id] read-account)
  (POST "/account/:id/deposit" [id] deposit)
  (POST "/account/:id/withdraw" [id] withdraw)
  (POST "/account/:id/send" [id] send-money)
  (GET  "/account/:id/audit" [id] get-log)
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
