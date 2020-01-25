(ns exercise-7-test
  (:require [aleph.http         :as http]
            [bank-api.core      :as core]
            [bank-api.memory    :as memory]
            bank-api.spec
            [byte-streams       :as bs]
            [clojure.data.json  :as json]
            [clojure.spec.alpha :as s]
            [clojure.test       :refer :all]
            [clj-gatling.core   :as clj-gatling]
            [fixtures           :as fixtures]
            [test-data          :as data]))

(use-fixtures :once fixtures/bootstrap-server)
(use-fixtures :each fixtures/blow-up-the-bank)
(def concurrency 1000)

; CREATE  ----------------------------------------------------------------------

(defn- make-account
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
  (testing "Server is running"
    (let [{:keys [body status]} @(http/get (str core/base-url "/status") {:throw-exceptions false})]
      (is (= (bs/to-string body) "OK"))
      (is (= status 200))))
  (testing (str "Create " concurrency " accounts")
    (let [{:keys [ok]} (clj-gatling/run creation-sim {:concurrency concurrency :requests concurrency})
          account-numbers (map :account-number @memory/open-accounts)]
      (is (= concurrency (inc @memory/free-account-number))) ;; account numbers start from 0
      (is (= concurrency (count @memory/open-accounts)))
      (is (= concurrency ok))
      (is (distinct? account-numbers)))))

; DEPOSIT ----------------------------------------------------------------------

(defn- fill-account [{:keys [user-id]}]
  (let [{:keys [body status]} @(http/post (str core/base-url "/account/" user-id "/deposit?amount=" (inc user-id)) ;; user-id starts from 0 and you cant deposit 0 dollars
                                          {:throw-exceptions false})
        {:keys [name balance account-number]} (bs/to-string body)]\
    (and (= balance account-number name)
         (= status 200))))

(def deposit-sim
  {:name "Customers opening accounts simulation"
   :scenarios [{:name "Deposit cash money"
                :steps [{:name "Make account"
                         :request make-account}
                        {:name "Deposit into account"
                         :request fill-account}]}]})

(defn- get-account [id]
  (let [{:keys [body status]} @(http/get (str core/base-url "/account/" id) {:throw-exceptions false})]
    (when (= 200 status)
      (bs/to-string body))))

(defn- k-str->k-keyword
  "json maps have their keys converted to strings instead of keywords.
   this function transforms kv pairs back to their clojure form."
  [[k v]]
  [(keyword k) v])

(deftest deposit-testing
  (testing "Server is running"
    (let [{:keys [body status]} @(http/get (str core/base-url "/status"))]
      (is (= (bs/to-string body) "OK"))
      (is (= status 200))))
  (testing (str "Deposit into " concurrency " accounts")
    (let [{:keys [ok]}    (clj-gatling/run deposit-sim {:concurrency concurrency :requests concurrency})
          poorest-account (->> 0               get-account json/read-str (map k-str->k-keyword) (into {}))
          richest-account (->> concurrency dec get-account json/read-str (map k-str->k-keyword) (into {}))
          n-reqs          (-> deposit-sim :scenarios first :steps count (* concurrency))]
      (is (s/valid? :bank-api.spec/customer-facing-account poorest-account))
      (is (s/valid? :bank-api.spec/customer-facing-account richest-account))
      (is (= n-reqs ok))
      (is (= 1 (:balance poorest-account)))
      (is (= concurrency (:balance richest-account))))))
