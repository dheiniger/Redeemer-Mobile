(ns redeemer-mobile.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [redeemer-mobile.events]
            [redeemer-mobile.subs]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def react-native-side-menu (r/adapt-react-class (.-default (js/require "react-native-side-menu"))))

(def logo-img (js/require "./images/logo.png"))
(def menu-img (js/require "./images/hamburger-icon.png"))

(defn alert [title]
  (.alert (.-Alert ReactNative) title))

(defn app-root []
  (let [greeting (subscribe [:get-greeting])]
    (fn []
      [view {:style {:width "100%" :height "100%"}}
       [header]
       [body
        [text {:key   "greeting-text"
               :style {:font-size 30 :font-weight "100" :text-align "center"}} @greeting]]])))

(defn header []
  [view {:style {:height 60}}
   [touchable-highlight {:on-press #(alert "Button Pressed")
                         :style    {:height 50 :width 50}}
    [image {:source menu-img
            :style  {:height 25 :width 35 :margin 8 :position "absolute" :resizeMode "contain" :flex 1 :left "0%" :top 0}}]]
   [image {:source logo-img
           :style  {:width 40 :height 60 :margin-top -5 :margin-right 5 :position "absolute" :resizeMode "contain" :flex 1 :right "0%" :top "0%"}}]])

(defn body [& content]
  [view {:style {:width "80%" :margin-left "auto" :margin-right "auto"}}
   content])

(defn menu-items []
  [view {:style {:color "gray"}}
   [text {:style {:color "white"}} "Element 1"]
   [text {:style {:color "white"}} "Element 2"]])

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "RedeemerMobile" #(r/reactify-component app-root)))
