(ns exercise-7-tests.deposit-concurrency-test
  (:require [aleph.http         :as http]
            [bank-api.core      :as core]
            bank-api.spec
            [clojure.spec.alpha :as s]
            [clojure.test       :refer :all]
            [clj-gatling.core   :as clj-gatling]
            [exercise-7-tests.create-concurrency-test :as create]
            [exercise-7-tests.utils :as utils]
            [fixtures           :as fixtures]
            [test-data          :as data]))

(use-fixtures :once fixtures/bootstrap-server)

; DEPOSIT ----------------------------------------------------------------------

(defn fill-account [{:keys [user-id]}]
  (let [{:keys [body status]} @(http/post (str core/base-url "/account/" user-id "/deposit?amount=" (inc user-id)) ;; user-id starts from 0 and you cant deposit 0 dollars
                                          {:throw-exceptions false})
        {:keys [balance account-number]} (utils/bs->clj body)]
    (and (= (dec balance) account-number)
         (= status 200))))

(def deposit-sim
  {:name "Customers depositing into accounts simulation"
   :scenarios [{:name "Deposit cash money"
                :steps [{:name "Make account"
                         :request create/make-account}
                        {:name "Deposit into account"
                         :request fill-account}]}]})

(deftest deposit-testing
  (testing (str "Deposit into " data/concurrency " accounts")
    (let [{:keys [ok]}    (clj-gatling/run deposit-sim {:concurrency data/concurrency :requests data/concurrency})
          poorest-account (->> 0                    utils/get-account)
          richest-account (->> data/concurrency dec utils/get-account)
          n-reqs          (-> deposit-sim :scenarios first :steps count (* data/concurrency))]
      (is (s/valid? :bank-api.spec/customer-facing-account poorest-account))
      (is (s/valid? :bank-api.spec/customer-facing-account richest-account))
      (is (= n-reqs ok))
      (is (= 1 (:balance poorest-account)))
      (is (= data/concurrency (:balance richest-account))))))
