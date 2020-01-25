(ns exercise-7-tests.utils
  (:require [aleph.http    :as http]
            [bank-api.core :as core]
            [byte-streams  :as bs]))
            
(defn k-str->k-keyword
  "json maps have their keys converted to strings instead of keywords.
  this function transforms kv pairs back to their clojure form."
  [[k v]]
  [(keyword k) v])

(defn get-account [id]
  (let [{:keys [body status]} @(http/get (str core/base-url "/account/" id) {:throw-exceptions false})]
    (when (= 200 status)
      (bs/to-string body))))
