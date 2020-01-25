(ns exercise-7-tests.deposit-dollar-concurrency-test
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

(use-fixtures :once fixtures/bootstrap-and-create-account)

(defn deposit-dollar [_]
  (let [{:keys [body status]} @(http/post (str core/base-url "/account/" data/black-account-number "/deposit?amount=" 1)
                                          {:throw-exceptions false})]
    (= status 200)))

(def deposit-dollar-sim
  {:name "Customer depositing 1 dollar into account simulation"
   :scenarios [{:name "Deposit dollar"
                :steps [{:name "Deposit dollar"
                         :request deposit-dollar}]}]})

(deftest deposit-dollars-testing
  ;;TODO deposit is not thread safe yet.
  #_(testing (str "Deposit 1 dollar " data/concurrency " times into a single account")
    (let [{:keys [ok]} (clj-gatling/run deposit-dollar-sim {:concurrency data/concurrency :requests data/concurrency})
          account      (utils/get-account 0)]
      (is (s/valid? :bank-api.spec/customer-facing-account account))
      (is (= data/concurrency ok))
      (is (= data/concurrency (:balance account))))))
