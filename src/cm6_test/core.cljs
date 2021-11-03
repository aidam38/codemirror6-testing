(ns cm6-test.core
  (:require [reagent.dom :as rd]
            [cm6-test.views :refer [app]]))

(defn ^:export clear-cache-and-render! []
  ())

(defn ^:export init []
  (rd/render [app] (.getElementById js/document "app")))