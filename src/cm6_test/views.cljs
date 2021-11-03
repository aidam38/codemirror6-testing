(ns cm6-test.views
  (:require [reagent.core :as r]
            [applied-science.js-interop :as j]
            ["@codemirror/state" :refer [EditorState]]
            ["@codemirror/view" :refer [EditorView keymap]]
            ["@codemirror/commands" :refer [defaultKeymap]]
            ["@codemirror/lang-javascript" :refer [javascript]]))

(defn editor []
  (r/with-let
    [mount!
     (fn [el]
       (new EditorView
            (j/obj :state
                   (.create EditorState
                            #{:extensions [(.of keymap defaultKeymap), (javascript)]})
                   :parent el)))]
    [:div
     {:ref mount!}]))

(defn app []
  [:div.w-96.h-48.m-auto.mt-24.bg-red-50
   [editor]])