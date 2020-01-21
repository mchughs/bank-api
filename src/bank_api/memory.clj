(ns bank-api.memory
  "All the bits of the app which are aware of the implementation of the app state.")

(def free-account-number (atom 0))
(def open-accounts (atom {}))

(defn blow-up-the-bank! []
  (reset! free-account-number 0)
  (reset! open-accounts {}))

(defn mutate-account! [account-number new-account]
  (swap! open-accounts assoc (str account-number) new-account))

(defn register-new-account!
  [{:keys [account-number] :as new-account}]
  (swap! free-account-number inc)
  (mutate-account! account-number new-account))

(defn get-account
  "returns nil if no account was found associated with the ID"
  [id]
  (get @open-accounts id))

(defn push-to-log! ;;TODO careful of race condition
  [{:keys [id debit credit description]}]
  (let [account     (get-account id)
        audit-log   (:audit-log account)
        sequence    (count audit-log)
        info        {:sequence sequence
                     :description description}
        transaction (if debit
                      {:debit debit}
                      {:credit credit})
        new-record  (merge transaction info)
        new-audit-log (conj audit-log new-record)
        new-account (assoc account :audit-log new-audit-log)]
    (mutate-account! id new-account)))
