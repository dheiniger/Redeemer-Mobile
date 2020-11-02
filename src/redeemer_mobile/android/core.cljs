(ns redeemer-mobile.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [redeemer-mobile.events]
            [redeemer-mobile.subs]
            [redeemer-mobile.common.view :refer [header]]
            [redeemer-mobile.common.util :as util]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)));;TODO Remove??
(def scrollview (r/adapt-react-class (.-ScrollView ReactNative)))


(defn body [menu-state & content]
  (let [height (if (= :open menu-state) 0 "auto")]
    [view {:key   "body"
           :style {:width "80%" :height height :margin-left "auto" :margin-right "auto" :z-index -10 :elevation -10}}
     content]))

;;TODO move these
;; -- Pages ------------------------------------------------------------
(defn home-page []
  [scrollview {:key   "home-page"}
   [text {:style (page-style)} "Home Page"]])

(defn learn-page []
  (requested-page "learn-page"))

(defn sermons-page []
  (requested-page "sermons-page"))

(defn blog-page []
  (requested-page "blog-page"))

(defn counseling-page []
  [scrollview {:key   "counseling-page"}
   [text {:style (page-style)} "Counseling"]])

(defn contact-page []
  [scrollview {:key   "contact-page"}
   [text {:style (page-style)} "Contact Us"]])

(defn page-style []
  {:font-size 30 :font-weight "100" :text-align "center"})

(defn requested-page[id]
  (let [page-content (subscribe [:get-page-content id])]
    [scrollview {:key   id}
     [text {:style (page-style)}  @page-content]]))

(defn pages []
  {:Home home-page
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
