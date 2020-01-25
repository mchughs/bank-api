(ns exercise-7-tests.utils
  (:require [aleph.http         :as http]
            [bank-api.core      :as core]
            [byte-streams       :as bs]
            [clojure.data.json  :as json]))

(defn k-str->k-keyword
  "json maps have their keys converted to strings instead of keywords.
  this function transforms kv pairs back to their clojure form."
  [[k v]]
  [(keyword k) v])

(defn bs->clj [body]
  (->> body
       bs/to-string
       json/read-str
       (map k-str->k-keyword)
       (into {})))

(defn get-account [id]
  (let [{:keys [body status]} @(http/get (str core/base-url "/account/" id) {:throw-exceptions false})]
    (when (= 200 status)
      (->> body bs->clj))))
