(ns redeemer-mobile.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::page keyword?)
(s/def ::menu-state keyword?)
(s/def ::app-db
  (s/keys :req-un [::page]))

;; initial state of app-db
(def app-db {:page   :Home
             :menu-state :closed
             :learn-page-content "Learn page"})
