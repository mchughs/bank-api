(ns resources.fixtures
  (:require [ring.mock.request :as mock]
            [bank-api.memory :as memory]
            [bank-api.handler :as handler]
            [resources.test-data :as data]))

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

(defn apply-sequence-of-transactions [f]
  (handler/app (mock/request :post (str "/account?name=" data/black-account-owner)))
  (handler/app (mock/request :post (str "/account/" data/black-account-number "/deposit?amount=" data/lots-of-money)))
  (handler/app (mock/request :post (str "/account?name=" data/white-account-owner)))
  (handler/app (mock/request :post (str "/account/" data/white-account-number "/deposit?amount=" data/lots-of-money)))
  (handler/app (mock/request :post (str "/account/" data/black-account-owner "/send?amount=" data/white-account-owner "&account-number=" data/lots-of-money)))
  (handler/app (mock/request :post (str "/account/" data/white-account-owner "/send?amount=" data/black-account-owner "&account-number=" data/lots-of-money)))
  (handler/app (mock/request :post (str "/account/" data/black-account-owner "/withdraw?amount=" data/lots-of-money)))
  (handler/app (mock/request :post (str "/account/" data/white-account-owner "/withdraw?amount=" data/lots-of-money)))
  (f)
  (memory/blow-up-the-bank!))
