(ns fixtures
  (:require [bank-api.core :as core]
            [bank-api.memory :as memory]
            [bank-api.handler :as handler]
            [ring.mock.request :as mock]
            [test-data :as data]))

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
  (handler/app (mock/request :post (str "/account/" data/black-account-number "/send?amount=" data/lots-of-money "&account-number=" data/white-account-number)))
  (handler/app (mock/request :post (str "/account/" data/white-account-number "/send?amount=" data/lots-of-money "&account-number=" data/black-account-number)))
  (handler/app (mock/request :post (str "/account/" data/black-account-number "/withdraw?amount=" data/lots-of-money)))
  (handler/app (mock/request :post (str "/account/" data/white-account-number "/withdraw?amount=" data/lots-of-money)))
  (f)
  (memory/blow-up-the-bank!))

(defn bootstrap-server [f]
  (let [server (core/start-server)]
    (f)
    (memory/blow-up-the-bank!)
    (core/stop-server server)))

(defn bootstrap-and-create-account [f]
  (let [server (core/start-server)]
    (handler/app (mock/request :post (str "/account?name=" data/black-account-owner)))
    (f)
    (memory/blow-up-the-bank!)
    (core/stop-server server)))

(defn bootstrap-and-enrich-account [f]
  (let [server (core/start-server)]
    (handler/app (mock/request :post (str "/account?name=" data/black-account-owner)))
    (handler/app (mock/request :post (str "/account/" data/black-account-number "/deposit?amount=" data/concurrency)))
    (f)
    (memory/blow-up-the-bank!)
    (core/stop-server server)))

(defn bootstrap-and-initialize [f]
  (let [server (core/start-server)]
    (dotimes [id data/concurrency]
      (handler/app (mock/request :post (str "/account?name=" data/black-account-owner)))
      (handler/app (mock/request :post (str "/account/" id "/deposit?amount=" data/lots-of-money))))
    (dotimes [id data/concurrency]
      (handler/app (mock/request :post (str "/account?name=" data/white-account-owner))))
    (f)
    (memory/blow-up-the-bank!)
    (core/stop-server server)))
