(ns redeemer-mobile.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [redeemer-mobile.events]
            [redeemer-mobile.subs]
            [redeemer-mobile.common.view :as display]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))
(def view (r/adapt-react-class (.-View ReactNative)))

(defn app-root []
  (let [page (subscribe [:get-page])
        menu-state (subscribe [:get-menu-state])]
    (fn []
      [view {:style {:width "100%" :height "100%"}}
       [display/header @menu-state]
       [display/body
        @menu-state
        ((display/get-page @page))]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "RedeemerMobile" #(r/reactify-component app-root)))
