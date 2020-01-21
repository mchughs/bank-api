(ns bank-api.exercise-2-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [bank-api.handler :as handler]
            [resources.test-data :as data]
            [resources.fixtures :as fixtures]))

(use-fixtures :each fixtures/register-black-account)

(deftest view-test
  (testing "Exercise 2: View newly created account."
    (let [{:keys [status body]} (handler/app (mock/request :get (str "/account/" data/black-account-number)))]
      (is (= status 200))
      (is (= body (json/write-str data/empty-black-account))))))
