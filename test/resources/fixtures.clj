(ns resources.fixtures
  (:require [ring.mock.request :as mock]
            [bank-api.memory :as memory]
            [bank-api.handler :as handler]
            [resources.test-data :as data]))
;;TODO move to resources
(defn register-black-account [f]
  (handler/app (mock/request :post (str "/account?name=" data/black-account-owner)))
  (f)
  (memory/blow-up-the-bank!))

(defn register-and-enrich-black-account [f]
  (handler/app (mock/request :post (str "/account?name=" data/black-account-owner)))
  (handler/app (mock/request :post (str "/account/" data/black-account-number "/deposit?amount=" data/lots-of-money)))
  (f)
  (memory/blow-up-the-bank!))

(defn register-and-enrich-black-and-white-account [f]
  (handler/app (mock/request :post (str "/account?name=" data/black-account-owner)))
  (handler/app (mock/request :post (str "/account/" data/black-account-number "/deposit?amount=" data/lots-of-money)))
  (handler/app (mock/request :post (str "/account?name=" data/white-account-owner)))
  (handler/app (mock/request :post (str "/account/" data/white-account-number "/deposit?amount=" data/lots-of-money)))
  (f)
  (memory/blow-up-the-bank!))
