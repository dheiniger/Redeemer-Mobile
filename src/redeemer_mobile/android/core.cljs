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
(def menu-width 310)

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
   [menu]
   [image {:source logo-img
           :style  {:width 40 :height 60 :margin-top -5 :margin-right 5 :position "absolute" :resizeMode "contain" :flex 1 :right "0%" :top "0%"}}]])

(defn body [& content]
  [view {:style {:width "80%" :margin-left "auto" :margin-right "auto" :background-color "green" :z-index -10 :elevation -10}}
   content])

;;TODO
(defn menu [state & items]
  [menu-open]
  )

(defn menu-closed []
  [view
   [touchable-highlight {:on-press #(alert "Button Pressed")
                         :style    {:height 50 :width 50}}
    [image {:source menu-img
            :style  {:height 25 :width 35 :margin 8 :position "absolute" :resizeMode "contain" :flex 1 :left "0%" :top 0}}]]])

(defn menu-open []
  [view {:style {:width menu-width :height 900 :z-index 10 :elevation 10 :background-color "white"}}
   [touchable-highlight {:on-press       #(alert "Button Pressed")
                         :style          {:height 50 :width 50 :left (- menu-width 50)}
                         :underlay-color "red"}
    [image {:source menu-img
            :style  {:height 25 :width 35 :position "absolute" :resizeMode "contain" :flex 1 :left "0%" :top 8}}]]
   [menu-items]])

(defn menu-items []
  [view
   [menu-item logo-img "Home"]
   [menu-item logo-img "Welcome"]
   [menu-item logo-img "About Us"]])

(defn menu-item [icon menu-text]
  [view {:style {:height 45 :z-index 11 :elevation 11}}
   [touchable-highlight {:on-press #(alert (str menu-text " Pressed!"))
                         :style    {:width menu-width :height 45}}
    [view {:style {:z-index 100 :elevation 100 :background-color "green"}}
     [image {:source icon
             :style  {:height 45 :width 40}}]
     [text {:style {:font-size 30 :flex 1 :position "absolute" :left 60}}
      menu-text]]]])

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "RedeemerMobile" #(r/reactify-component app-root)))
