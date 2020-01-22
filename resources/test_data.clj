(ns test-data)

(def black-account-owner "Mr.Black")
(def white-account-owner "Ms.White")
(def black-account-number 0)
(def white-account-number 1)
(def lots-of-money 123456789)
(def fake-account-number 666)

(def empty-black-account
  {:name           black-account-owner
   :balance        0
   :account-number black-account-number})

(def empty-white-account
  {:name           white-account-owner
   :balance        0
   :account-number white-account-number})

(def rich-black-account (assoc empty-black-account :balance lots-of-money))
(def rich-white-account (assoc empty-white-account :balance lots-of-money))
(def super-rich-black-account (assoc empty-black-account :balance (* 2 lots-of-money)))
(def super-rich-white-account (assoc empty-white-account :balance (* 2 lots-of-money)))

(def black-audit-log [
  {:sequence 3
   :debit 123456789
   :description "withdraw"}
  {:sequence 2
   :credit 123456789
   :description (str "received from #" white-account-number)}
  {:sequence 1
   :debit 123456789
   :description (str "sent to #" white-account-number)}
  {:sequence 0
   :credit 123456789
   :description "deposit"}])

(def white-audit-log [
  {:sequence 3
   :debit 123456789
   :description "withdraw"}
  {:sequence 2
   :debit 123456789
   :description (str "sent to #" black-account-number)}
  {:sequence 1
   :credit 123456789
   :description (str "received from #" black-account-number)}
  {:sequence 0
   :credit 123456789
   :description "deposit"}])
