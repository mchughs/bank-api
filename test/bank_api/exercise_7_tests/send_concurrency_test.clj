(ns exercise-7-tests.send-concurrency-test
  (:require [aleph.http         :as http]
            [bank-api.core      :as core]
            bank-api.spec
            [clojure.spec.alpha :as s]
            [clojure.test       :refer :all]
            [clj-gatling.core   :as clj-gatling]
            [exercise-7-tests.create-concurrency-test :as create]
            [exercise-7-tests.deposit-concurrency-test :as deposit]
            [exercise-7-tests.utils :as utils]
            [fixtures           :as fixtures]
            [test-data          :as data]))

(use-fixtures :once fixtures/bootstrap-and-initialize)

; SEND ----------------------------------------------------------------------

(defn send-money [{:keys [user-id]}]
  (let [{:keys [body status]} @(http/post (str core/base-url "/account/" user-id
                                                             "/send?amount=" data/lots-of-money
                                                             "&account-number=" (+ data/concurrency user-id))
                                          {:throw-exceptions false})
        {:keys [balance]} (utils/bs->clj body)]
    (and (= balance 0) ;; balance should be drained after the send-off
         (= status 200))))

(def send-sim
  {:name "Customers with money send to customers without money simulation."
   :scenarios [{:name "send money to your poor counter-part"
                :steps [{:name "Send money to your poor counter-part"
                         :request send-money}]}]})

(deftest send-testing
  (testing (str "Net transfer from first half of accounts to second half of accounts.")
    (let [{:keys [ok]} (clj-gatling/run send-sim {:concurrency data/concurrency :requests data/concurrency})
          first-account    (->> 0                          utils/get-account)
          middle-account-1 (->> data/concurrency dec       utils/get-account)
          middle-account-2 (->> data/concurrency           utils/get-account)
          last-account     (->> data/concurrency (* 2) dec utils/get-account)]
      (is (s/valid? :bank-api.spec/customer-facing-account first-account))
      (is (s/valid? :bank-api.spec/customer-facing-account middle-account-1))
      (is (s/valid? :bank-api.spec/customer-facing-account middle-account-2))
      (is (s/valid? :bank-api.spec/customer-facing-account last-account))
      (is (= data/concurrency ok))
      (is (= 0 (:balance first-account)))
      (is (= 0 (:balance middle-account-1)))
      (is (= data/lots-of-money (:balance last-account))
      (is (= data/lots-of-money (:balance middle-account-2)))))))
