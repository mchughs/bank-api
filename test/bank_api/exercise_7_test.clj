(ns exercise-7-test
  (:require [aleph.http :as http]
            [bank-api.core :as core]
            [bank-api.memory :as memory]
            [byte-streams :as bs]
            [clj-gatling.core :as clj-gatling]
            [clojure.data.json :as json]
            [clojure.test :refer :all]
            [fixtures :as fixtures]
            [test-data :as data]))

(use-fixtures :each fixtures/bootstrap-server)

(defn- make-account [account-owner {:keys [user-id]}]
  (let [{:keys [body status]} @(http/post (str core/base-url "/account?name=" account-owner user-id))]
    (= status 200)))

(def simulation
  {:name "Customers opening accounts simulation"
   :scenarios [{:name "Enter my name as Ms. White"
                :steps [{:name "Make account"
                         :request (partial make-account data/white-account-owner)}]}
               {:name "Enter my name as Mr. Black"
                :steps [{:name "Make account"
                         :request (partial make-account data/black-account-owner)}]}]})

(def concurrency 500)

(defn- all-reqs-successful?
  "Gatling can sometimes sneak in an extra request or two so we can't test for pure equality."
  [num-of-passes sim num-of-users]
  (let [num-of-scenarios      (-> sim :scenarios count)
        total-num-of-requests (* num-of-scenarios num-of-users)]
    (<= total-num-of-requests num-of-passes)))

(deftest load-testing
  (testing "Server is running"
    (let [{:keys [body status]} @(http/get (str core/base-url "/status"))]
      (is (= (bs/to-string body) "OK"))
      (is (= status 200))))
  (testing "Create 1 account"
    (let [{:keys [ok]} (clj-gatling/run simulation {:concurrency concurrency :requests concurrency})]
      (is (all-reqs-successful? ok simulation concurrency))
      (is (= 1000;;TODO
             (count @memory/open-accounts))))))
