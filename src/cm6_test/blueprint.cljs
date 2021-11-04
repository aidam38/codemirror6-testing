(ns cm6-test.blueprint
  (:require [reagent.core :as r]
            ["@blueprintjs/core" :as bp-core]
            ["@blueprintjs/select" :as bp-select]))

(def button (r/adapt-react-class bp-core/Button))

(def select (r/adapt-react-class bp-select/Select))

(def menu-item* (r/adapt-react-class bp-core/MenuItem))

;; blueprint 3.50.0 introduced tabindex on menu-items
;; which causes them to autofocus when they render
;; and have a blue outline around them
(defn menu-item [props & children]
  (let [props (merge
               (when (map? props)
                 props)
               (if (seq children)
                  ;; nil won't override the tab-index if it has children
                  ;; have no idea why this is, but -1 fixes the element getting focused
                 {:tab-index -1}
                 {:tab-index nil}))]
    (into [menu-item* props]
          children)))

;; use cljs.bean
(defn select-wrapper [{:as   props
                       :keys [item-renderer
                              on-item-select]}
                      child]
  [select
   (merge props
          {:item-renderer  (fn [js-item js-props]
                             (let [item  (js->clj js-item :keywordize-keys true)
                                   props (js->clj js-props :keywordize-keys true)]
                               (r/as-element
                                ^{:key (str item)}
                                [item-renderer item props])))
           :on-item-select (fn [js-item evt]
                             (on-item-select (js->clj js-item :keywordize-keys true) evt))})
   child])
