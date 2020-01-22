(ns exercise-4-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [bank-api.handler :as handler]
            [test-data :as data]
            [fixtures :as fixtures]))

(use-fixtures :each fixtures/register-and-enrich-black-account)

(defn- withdraw-money
  ([] (withdraw-money {}))
  ([{:keys [account-number amount]
     :or {account-number data/black-account-number
          amount         data/lots-of-money}}]
   (handler/app (mock/request :post (str "/account/" account-number "/withdraw?amount=" amount)))))

(deftest withdraw-test
  (testing "Exercise 4: Successfully withdraw positive sum from newly enriched account."
    (let [{:keys [status body]} (withdraw-money)]
      (is (= status 200))
      (is (= body (json/write-str data/empty-black-account))))))

(deftest fail-withdraw-tests
  (testing "Exercise 4: Fail to withdraw positive sum in unknown account."
    (let [{:keys [status body]} (withdraw-money {:account-number data/fake-account-number})]
      (is (= status 400))
      (is (= body (format "No account associated with account-number %d." data/fake-account-number)))))

  (testing "Exercise 4: Fail to withdraw 0 sum from account."
    (let [{:keys [status body]} (withdraw-money {:amount 0})]
      (is (= status 400))
      (is (= body "The amount (0) cannot be withdrawn from an account."))))

  (testing "Exercise 4: Fail to withdraw negative sum from account."
    (let [{:keys [status body]} (withdraw-money {:amount -1})]
      (is (= status 400))
      (is (= body "The amount (-1) cannot be withdrawn from an account."))))

  (testing "Exercise 4: Fail to withdraw more money than exists in the account."
    (let [{:keys [status body]} (withdraw-money {:amount (* 2 data/lots-of-money)})]
      (is (= status 400))
      (is (= body (format "The balance of an account cannot go as low as (-%d)." data/lots-of-money))))))
