(ns redeemer-mobile.common.util
  (:require [clojure.string :as str]))

;;TODO remove this?
(defn make-content-keyword [page-key]
  (keyword (str/replace (str page-key "-content") ":" "")))