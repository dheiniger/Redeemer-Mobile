(ns redeemer-mobile.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [redeemer-mobile.events]
            [redeemer-mobile.subs]
            [redeemer-mobile.common.view :refer [header]]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))


(defn app-root []
  (let [content (subscribe [:get-content])
        state (subscribe [:get-menu-state])]
    (fn []
      [view {:style {:width "100%" :height "100%"}}
       [header @state]
       [body
        @state                                              ;;TODO HACKY, WILL TRY TO FIX EVENTUALLY
        [text {:key   "content"
               :style {:font-size 30 :font-weight "100" :text-align "center"}} @content]]])))

(defn body [menu-state & content]
  (let [height (if (= "open" menu-state) 0 "auto")]
    [view {:style {:width "80%" :height height :margin-left "auto" :margin-right "auto" :z-index -10 :elevation -10}}
     content]))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "RedeemerMobile" #(r/reactify-component app-root)))
