(ns bank-api.utils
  (:require [clojure.data.json :as json]))

(defn- hide-log [account]
  (dissoc account :audit-log))

(defn customer-safe-response [res]
  (-> res hide-log json/write-str))

(defn bad-request [msg]
  {:status 400 :body msg})

(defn missing-account-response [id]
  (bad-request (format "No account associated with account-number %s." id)))

(defn append-log
  [{:keys [audit-log] :as account} {:keys [id debit credit description] :as log}]
  (let [info          {:sequence    (count audit-log)
                       :description description}
        transaction   (if debit
                        {:debit debit}
                        {:credit credit})
        new-record    (merge transaction info)
        new-audit-log (conj audit-log new-record)]
    (assoc account :audit-log new-audit-log)))
