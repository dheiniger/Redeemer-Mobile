(ns redeemer-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :get-content
  (fn [db _]
    (:content db)))

(reg-sub
  :get-menu-state
  (fn [db _]
    (:menu-state db)))
