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


(defn body [menu-state & content]
  (let [height (if (= :open menu-state) 0 "auto")]
    [view {:key   "body"
           :style {:width "80%" :height height :margin-left "auto" :margin-right "auto" :z-index -10 :elevation -10}}
     content]))

;; -- Pages ------------------------------------------------------------
(defn home-page []
  [view {:key   "home-page"}
   [text {:style (page-style)} "Home Page"]])

(defn welcome-page []
  [view {:key   "home-page"}
   [text {:style (page-style)} "Welcome Page"]])

(defn learn-page []
  [view {:key   "learn-page"}
   [text {:style (page-style)} "Learn Page"]])

(defn sermons-page []
  [view {:key   "sermons-page"}
   [text {:style (page-style)} "Sermons"]])

(defn blog-page []
  [view {:key   "blog-page"}
   [text {:style (page-style)} "Blog"]])

(defn counseling-page []
  [view {:key   "counseling-page"}
   [text {:style (page-style)} "Counseling"]])

(defn contact-page []
  [view {:key   "contact-page"}
   [text {:style (page-style)} "Contact Us"]])

(defn page-style []
  {:font-size 30 :font-weight "100" :text-align "center"})

(defn pages []
  {:Home home-page
   :Welcome welcome-page
   :Learn learn-page
   :Sermons sermons-page
   :Blog blog-page
   :Counseling counseling-page
   :Contact contact-page})

(defn get-page [page-name]
  (or (page-name (pages)) home-page))

;; --------------------------------------------------------------

(defn app-root []
  (let [page (subscribe [:get-page])
        menu-state (subscribe [:get-menu-state])]
    (fn []
      [view {:style {:width "100%" :height "100%"}}
       [header @menu-state]
       [body
        @menu-state
        ((get-page @page))]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "RedeemerMobile" #(r/reactify-component app-root)))
