(ns exercise-7-tests.create-concurrency-test
  (:require [aleph.http         :as http]
            [bank-api.core      :as core]
            [bank-api.memory    :as memory]
            [byte-streams       :as bs]
            [clojure.test       :refer :all]
            [clj-gatling.core   :as clj-gatling]
            [fixtures           :as fixtures]
            [test-data          :as data]))

(use-fixtures :once fixtures/bootstrap-server)

; CREATE  ----------------------------------------------------------------------

(defn make-account
  ([sim-data] (make-account "" sim-data))
  ([account-owner {:keys [user-id]}]
  (let [{:keys [body status]} @(http/post (str core/base-url "/account?name=" account-owner user-id) {:throw-exceptions false})]
    (= status 200))))

(def creation-sim
  {:name "Customers opening accounts simulation"
   :scenarios [{:name "Enter my name as Ms. White"
                :steps [{:name "Make account"
                         :request (partial make-account data/white-account-owner)}]}
               {:name "Enter my name as Mr. Black"
                :steps [{:name "Make account"
                         :request (partial make-account data/black-account-owner)}]}]})

(deftest creation-testing
  (testing (str "Create " data/concurrency " accounts")
    (let [{:keys [ok]} (clj-gatling/run creation-sim {:concurrency data/concurrency :requests data/concurrency})
          account-numbers (map :account-number @memory/open-accounts)]
      (is (= data/concurrency (inc @memory/free-account-number))) ;; account numbers start from 0
      (is (= data/concurrency (count @memory/open-accounts)))
      (is (= data/concurrency ok))
      (is (distinct? account-numbers)))))
