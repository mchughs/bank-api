(ns bank-api.handler
  (:require [bank-api.audit    :as audit]
            [bank-api.create   :as create]
            [bank-api.memory   :as memory]
            [bank-api.read     :as read]
            [bank-api.send     :as send]
            [bank-api.transact :as transact]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defroutes app-routes
  (POST "/account" [] create/create-account)
  (GET  "/account/:id" [id] read/read-account)
  (POST "/account/:id/deposit" [id] transact/deposit)
  (POST "/account/:id/withdraw" [id] transact/withdraw)
  (POST "/account/:id/send" [id] send/send-money)
  (GET  "/account/:id/audit" [id] audit/get-log)
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
