(ns bank-api.exercise-3-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [bank-api.handler :as handler]
            [resources.test-data :as data]
            [resources.fixtures :as fixtures]))

(use-fixtures :each fixtures/register-black-account)

(defn- deposit-money
  ([] (deposit-money {}))
  ([{:keys [account-number amount]
     :or {account-number data/black-account-number
          amount         data/lots-of-money}}]
   (handler/app (mock/request :post (str "/account/" account-number "/deposit?amount=" amount)))))

(deftest deposit-test
  (testing "Exercise 3: Successfully deposit positive sum in account."
    (let [{:keys [status body]} (deposit-money)]
      (is (= status 200))
      (is (= body (json/write-str data/rich-black-account)))))

  (testing "Exercise 3: Fail to deposit positive sum in unknown account."
    (let [{:keys [status body]} (deposit-money {:account-number data/fake-account-number})]
      (is (= status 400))
      (is (= body (format "No account associated with account-number %d." data/fake-account-number)))))

  (testing "Exercise 3: Fail to deposit 0 sum in account."
    (let [{:keys [status body]} (deposit-money {:amount 0})]
      (is (= status 400))
      (is (= body "The amount (0) cannot be deposited into an account."))))

  (testing "Exercise 3: Fail to deposit negative sum in account."
    (let [{:keys [status body]} (deposit-money {:amount -1})]
      (is (= status 400))
      (is (= body "The amount (-1) cannot be deposited into an account.")))))
