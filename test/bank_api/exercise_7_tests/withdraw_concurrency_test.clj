(ns exercise-7-tests.withdraw-concurrency-test
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

(use-fixtures :once fixtures/bootstrap-server)

; WITHDRAW ----------------------------------------------------------------------

(defn- drain-account [{:keys [user-id]}]
  (let [{:keys [body status]} @(http/post (str core/base-url "/account/" user-id "/withdraw?amount=" (inc user-id)) ;; user-id starts from 0 and you cant withdraw 0 dollars
                                          {:throw-exceptions false})
        {:keys [balance]} (utils/bs->clj body)]
    (and (= balance 0)
         (= status 200))))

(def withdraw-sim
  {:name "Customers withdrawing from accounts simulation"
   :scenarios [{:name "Deposit cash money"
                :steps [{:name "Make account"
                         :request create/make-account}
                        {:name "Deposit into account"
                         :request deposit/fill-account}
                        {:name "Withdraw from account"
                         :request drain-account}]}]})

(deftest withdraw-testing
  (testing (str "Withdraw from " data/concurrency " accounts")
    (let [{:keys [ok]}  (clj-gatling/run withdraw-sim {:concurrency data/concurrency :requests data/concurrency})
          first-account (->> 0                    utils/get-account)
          last-account  (->> data/concurrency dec utils/get-account)
          n-reqs        (-> withdraw-sim :scenarios first :steps count (* data/concurrency))]
      (is (s/valid? :bank-api.spec/customer-facing-account first-account))
      (is (s/valid? :bank-api.spec/customer-facing-account last-account))
      (is (= n-reqs ok))
      (is (= 0 (:balance first-account)))
      (is (= 0 (:balance last-account))))))
