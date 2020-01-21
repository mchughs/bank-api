(ns bank-api.handler
  (:require [bank-api.memory :as memory]
            [clojure.data.json :as json]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

;;TODOs spec stuff, db

(defn create-account [{:keys [params]}]
(let [{:keys [name]} params
      new-account    {:name           name
                      :balance        0
                      :account-number @memory/free-account-number}]
  (memory/register-new-account! new-account)
  (json/write-str new-account)))

(defn- view-account [{:keys [route-params]}]
  (let [{:keys [id]} route-params
        account (memory/get-account id)]
    (json/write-str account)))

(defn- deposit [{:keys [route-params params]}]
  (let [{:keys [id]} route-params
        {:keys [amount]} params
        amount (Integer. amount)]
    (if-not (pos? amount) ;;TODO replace with spec-check
      {:status 400 :body (format "The amount (%d) cannot be deposited into an account." amount)}
      (if-let [account (memory/get-account id)]
        (let [new-balance (+ (:balance account)
                          amount)
              new-account-data (assoc account :balance new-balance)]
          (memory/mutate-account! id new-account-data)
          (json/write-str new-account-data))
        {:status 400 :body (format "No account associated with account-number %s." id)}))))

(defn- withdraw [{:keys [route-params params]}]
  (let [{:keys [id]} route-params
        {:keys [amount]} params
        amount (Integer. amount)]
    (if-not (pos? amount) ;;TODO replace with spec-check
      {:status 400 :body (format "The amount (%d) cannot be withdrawn from an account." amount)}
      (if-let [account (memory/get-account id)]
        (let [new-balance (- (:balance account)
                             amount)]
          (if (neg? new-balance)
            {:status 400 :body (format "The balance of an account cannot go as low as (%d)." new-balance)}
            (let [new-account-data (assoc account :balance new-balance)]
              (memory/mutate-account! id new-account-data)
              (json/write-str new-account-data))))
        {:status 400 :body (format "No account associated with account-number %s." id)}))))

(defn- send-money [{:keys [route-params params]}]
  (let [{:keys [id]} route-params
        {:keys [account-number amount]} params]
    (if (= id account-number)
      {:status 400 :body "Can't send money to own account."}
      (let [withdraw-req {:route-params {:id id}
                          :params {:amount amount}}
            withdraw-res (withdraw withdraw-req)
            sender-err? (= (:status withdraw-res) 400)]
        (if sender-err?
          withdraw-res
          (let [deposit-req {:route-params {:id account-number}
                             :params {:amount amount}}
                deposit-res (deposit deposit-req)
                receiver-err? (= (:status deposit-res) 400)]
            (if receiver-err?
              deposit-res
              withdraw-res)))))))

(defroutes app-routes
  (POST "/account" [] create-account)
  (GET  "/account/:id" [id] view-account)
  (POST "/account/:id/deposit" [id] deposit)
  (POST "/account/:id/withdraw" [id] withdraw)
  (POST "/account/:id/send" [id] send-money)
  (GET "/account/:id/audit"
    [id] "TODO")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
