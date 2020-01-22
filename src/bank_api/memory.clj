(ns bank-api.memory
  "All the bits of the app which are aware of the implementation of the app state.")

(def free-account-number (atom 0))
(def open-accounts (atom {}))

(defn blow-up-the-bank! []
  (reset! free-account-number 0)
  (reset! open-accounts {}))

(defn mutate-account! [id new-account]
  (swap! open-accounts assoc (str id) new-account))

(defn register-new-account!
  [{:keys [account-number] :as new-account}]
  (swap! free-account-number inc)
  (mutate-account! account-number new-account))

(defn get-account
  "returns nil if no account was found associated with the ID"
  [id]
  (get @open-accounts id))
