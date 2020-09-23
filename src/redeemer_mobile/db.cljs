(ns redeemer-mobile.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::greeting string?)
(s/def ::app-db
  (s/keys :req-un [::greeting]))

;; initial state of app-db
(def app-db {:greeting   "Welcome to Redeemer Baptist Church of Norwalk!"
             :menu-state "closed"})
