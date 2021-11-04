(ns cm6-test.views
  (:require [reagent.core :as r]
            [applied-science.js-interop :as j]
            ["@codemirror/basic-setup" :refer [EditorState EditorView basicSetup]]
            ["@codemirror/lang-php" :refer [php]]))

(defn editor []
  (r/with-let
    [mount! (fn [el]
              (when el
                (new EditorView
                     (j/obj :state (.create EditorState
                                            #js{:extensions
                                                #js[basicSetup
                                                    (php)
                                                    (.theme EditorView #js{".cm-scroller" #js{:font-family "Fira Code"}})]})
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