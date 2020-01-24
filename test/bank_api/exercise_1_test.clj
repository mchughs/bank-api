(ns exercise-1-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [bank-api.handler :as handler]
            [test-data :as data]
            [fixtures :as fixtures]))

(use-fixtures :each fixtures/register-black-account)

(deftest no-route
  (testing "not-found route"
    (let [response (handler/app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

(deftest status-route
  (testing "status route"
    (let [response (handler/app (mock/request :get "/status"))]
      (is (= (:status response) 200)))))

(deftest create-test
  (testing "Exercise 1: Create second new account with unique account-number."
    (let [{:keys [status body]} (handler/app (mock/request :post (str "/account?name=" data/white-account-owner)))]
      (is (= status 200))
      (is (= body (json/write-str data/empty-white-account))))))
