(ns bank-api.exercise-5-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [bank-api.handler :as handler]
            [resources.test-data :as data]
            [resources.fixtures :as fixtures]))

(use-fixtures :each fixtures/register-and-enrich-black-and-white-account)

(defn- send-money
  [{:keys [from to amount]
    :or {amount data/lots-of-money}}]
  (handler/app (mock/request :post (str "/account/" from "/send?amount=" amount "&account-number=" to))))

(deftest send-black->white-test
  (testing "Exercise 5: Successfully send money from black to white."
    (let [{:keys [status body]} (send-money {:from data/black-account-number :to data/white-account-number})]
      (is (= status 200))
      (is (= body (json/write-str data/empty-black-account))
      (let [{:keys [status body]} (handler/app (mock/request :get (str "/account/" data/white-account-number)))]
        (is (= status 200))
        (is (= body (json/write-str data/super-rich-white-account))))))))

(deftest send-white->black-test
  (testing "Exercise 5: Successfully send money from white to black."
    (let [{:keys [status body]} (send-money {:from data/white-account-number :to data/black-account-number})]
      (is (= status 200))
      (is (= body (json/write-str data/empty-white-account))
      (let [{:keys [status body]} (handler/app (mock/request :get (str "/account/" data/black-account-number)))]
        (is (= status 200))
        (is (= body (json/write-str data/super-rich-black-account))))))))

(deftest missing-accounts-test
  (testing "Exercise 5: Fail to send from unknown account."
    (let [{:keys [status body]} (send-money {:from data/fake-account-number :to data/black-account-number})]
      (is (= status 400))
      (is (= body (format "No account associated with account-number %d." data/fake-account-number)))))
  (testing "Exercise 5: Fail to send to unknown account."
    (let [{:keys [status body]} (send-money {:from data/black-account-number :to data/fake-account-number})]
      (is (= status 400))
      (is (= body (format "No account associated with account-number %d." data/fake-account-number))))))

(deftest self-send-test
  (testing "Exercise 5: Fail to send to own accont."
    (let [{:keys [status body]} (send-money {:from data/black-account-number :to data/black-account-number})]
      (is (= status 400))
      (is (= body "Can't send money to own account.")))))

(deftest extra-send-tests
  (testing "Exercise 5: Fail to send negative sum between accounts."
    (let [{:keys [status body]} (send-money {:from data/black-account-number :to data/white-account-number :amount -1})]
      (is (= status 400))
      (is (= body "The amount (-1) cannot be withdrawn from an account."))))

  (testing "Exercise 5: Fail to send more money than exists in the account."
    (let [{:keys [status body]} (send-money {:from data/black-account-number :to data/white-account-number :amount (* 2 data/lots-of-money)})]
      (is (= status 400))
      (is (= body (format "The balance of an account cannot go as low as (-%d)." data/lots-of-money))))))
