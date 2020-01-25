(ns exercise-7-tests.create-concurrency-test
  (:require [aleph.http   :as http]
            [clojure.test :refer :all]
            [fixtures     :as fixtures]))

(use-fixtures :once fixtures/bootstrap-server)

(deftest server-test
  (testing "Server is running"
    (let [{:keys [body status]} @(http/get (str core/base-url "/status") {:throw-exceptions false})]
      (is (= (bs/to-string body) "OK"))
      (is (= status 200))))
