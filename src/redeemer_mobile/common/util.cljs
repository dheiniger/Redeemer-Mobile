(ns redeemer-mobile.common.util
  (:require [clojure.string :as str]))

(defn make-content-keyword [page-key]
  (keyword (str/replace (str page-key "-page-content") ":" "")))