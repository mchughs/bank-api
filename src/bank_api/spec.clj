(ns bank-api.spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::debit nat-int?)
(s/def ::credit nat-int?)
(s/def ::description string?)
(s/def ::sequence nat-int?)

(s/def ::log (s/or :deposit  (s/keys :req [::sequence ::description ::credit])
                   :withdraw (s/keys :req [::sequence ::description ::debit])))


(s/def ::audit-log (s/coll-of ::log :kind vector?))
(s/def ::account-number nat-int?)
(s/def ::balance nat-int?)
(s/def ::name string?)

(s/def ::account (s/keys :req-un [::name ::balance ::account-number ::audit-log]))

(s/def ::transaction-amount pos-int?)
