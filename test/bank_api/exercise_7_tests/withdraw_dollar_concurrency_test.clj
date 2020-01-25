(ns exercise-7-tests.withdraw-dollar-concurrency-test
  (:require [aleph.http         :as http]
            [bank-api.core      :as core]
            bank-api.spec
            [bank-api.handler   :as handler]
            [ring.mock.request  :as mock]
            [clojure.spec.alpha :as s]
            [clojure.test       :refer :all]
            [clj-gatling.core   :as clj-gatling]
            [exercise-7-tests.utils :as utils]
            [fixtures           :as fixtures]
            [test-data          :as data]))

(use-fixtures :once fixtures/bootstrap-and-enrich-account)

(defn withdraw-dollar [_]
  (let [{:keys [body status]} @(http/post (str core/base-url "/account/" data/black-account-number "/withdraw?amount=" 1)
                                          {:throw-exceptions false})]
    (= status 200)))

(def withdraw-dollar-sim
  {:name "Customer withdrawing 1 dollar into account simulation"
   :scenarios [{:name "withdraw dollar"
                :steps [{:name "withdraw dollar"
                         :request withdraw-dollar}]}]})

(deftest withdraw-dollars-testing
  ;;TODO withdraw is not threadsafe yet
  (testing (str "withdraw 1 dollar " data/concurrency " times from a single account")
    (let [{:keys [ok]} (clj-gatling/run withdraw-dollar-sim {:concurrency data/concurrency :requests data/concurrency})
          account      (utils/get-account 0)]
      (is (s/valid? :bank-api.spec/customer-facing-account account))
      (is (= data/concurrency ok))
      (is (= 0 (:balance account))))))
