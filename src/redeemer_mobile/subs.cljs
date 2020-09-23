(ns redeemer-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :get-greeting
  (fn [db _]
    (:greeting db)))

(reg-sub
  :get-menu-state
  (fn [db _]
    (:menu-state db)))
