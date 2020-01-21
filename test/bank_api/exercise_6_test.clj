(ns bank-api.exercise-5-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [bank-api.handler :as handler]
            [resources.test-data :as data]
            [resources.fixtures :as fixtures]))

(use-fixtures :each fixtures/apply-sequence-of-transactions)

(defn audit [id]
  (handler/app (mock/request :get (str "/account/" id "/audit"))))

(deftest audit-test
  (testing "Exercise 6: Successfully audit newly created black account."
    (let [{:keys [status body]} (audit data/black-account-number)]
      (is (= status 200))
      (is (= body (json/write-str data/black-audit-log)))))
  (testing "Exercise 6: Successfully audit newly created white account."
    (let [{:keys [status body]} (audit data/white-account-number)]
      (is (= status 200))
      (is (= body (json/write-str data/white-audit-log)))))
  (testing "Exercise 6: Fail to audit unknown account."
    (let [{:keys [status body]} (audit data/fake-account-number)]
      (is (= status 400))
      (is (= body (format "No account associated with account-number %d." data/fake-account-number))))))
