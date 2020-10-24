(ns redeemer-mobile.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::content string?)
(s/def ::app-db
  (s/keys :req-un [::content]))

;; initial state of app-db
(def app-db {:content   "Welcome to Redeemer Baptist Church of Norwalk!"
             :menu-state "closed"})
