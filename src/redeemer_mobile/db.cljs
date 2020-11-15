(ns redeemer-mobile.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::page keyword?)
(s/def ::menu-state keyword?)
(s/def ::app-db
  (s/keys :req-un [::page]))

;;TODO initialize data like blog post pages, that way I might be able to get rid of checks in the views
;;TODO make sure it would work if I addd new pages, etc.  Don't necessarily want to have to update this for every
;;TODO new page
;; initial state of app-db
(def app-db {:page   :Home
             :menu-state :closed})
