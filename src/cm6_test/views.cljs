(ns cm6-test.views
  (:require [reagent.core :as r]
            [applied-science.js-interop :as j]
            ["@codemirror/state" :refer [EditorState]]
            ["@codemirror/view" :refer [EditorView keymap drawSelection]]
            ["@codemirror/fold" :refer [foldGutter]]
            ["@codemirror/commands" :refer [defaultKeymap]]
            ["@codemirror/history" :refer [history historyKeymap]]
            ["@codemirror/highlight" :refer [defaultHighlightStyle]]
            ["@codemirror/gutter" :refer [lineNumbers]]
            ["@codemirror/matchbrackets" :refer [bracketMatching]]
            ["@codemirror/closebrackets" :refer [closeBrackets]]
            ["@codemirror/lang-javascript" :refer [javascript]]))

(defn editor []
  (r/with-let
    [extensions #js[(.of keymap
                         (concat defaultKeymap historyKeymap))
                    (.. EditorView -lineWrapping)
                    (.. EditorState -allowMultipleSelections (of true))
                    (drawSelection)
                    (bracketMatching)
                    (closeBrackets)
                    (history)
                    defaultHighlightStyle
                    (lineNumbers)
                    (foldGutter)
                    (javascript)]
     mount! (fn [el]
              (when el
                (new EditorView
                     (j/obj :state (.create EditorState #js{:extensions extensions})
                            :parent el))))]
    [:div
     {:ref mount!}]))

(defn lang-chooser []
  [:div.absolute])

(defn full-editor []
  [:<>
   [lang-chooser]
   [editor]])

(defn app []
  [:div.w-96.h-48.m-auto.mt-24
   [:div.rounded-md.overflow-auto.relative.border.shadow-lg.bg-white
    [full-editor]]])