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
            ["@codemirror/stream-parser" :refer [StreamLanguage]]
            ["@codemirror/lang-javascript" :refer [javascript]]
            #_["@codemirror/lang-html" :refer [html]]
            #_["@codemirror/lang-css" :refer [css]]
            #_["@codemirror/lang-json" :refer [json]]
            #_["@codemirror/lang-markdown" :refer [markdown]]
            #_["@codemirror/lang-java" :refer [java]]
            #_["@codemirror/lang-cpp" :refer [cpp]]
            #_["@codemirror/lang-rust" :refer [rust]]
            #_["@codemirror/lang-python" :refer [python]]
            #_["@codemirror/lang-php" :refer [php]]
            #_["@codemirror/lang-rust" :refer [rust]]
            #_["@codemirror/lang-rust" :refer [rust]]
            #_["@codemirror/legacy-modes/mode/lua" :refer [lua]]
            [clojure.string :as str]
            [cm6-test.blueprint :as blueprint]))

;; util
(defn find-some [pred coll]
  (some #(when (pred %) %) coll))

#_(def code-modes
    [["javascript" "javascript"]
     ["clojure" "clojure"]
     ["css" "css"]
     ["elixir", "elixir"]
     ["html" "htmlmixed"]
     ["plain text" "text/plain"]
     ["python" "python"]
     ["ruby" "ruby"]
     ["swift" "swift"]
     ["typescript" "text/typescript"]
     ["jsx" "jsx"]
     ["yaml" "yaml"]
     ["rust" "rust"]
     ["r" "r"]
     ["shell" "shell"]
     ["php" "php"]
     ["java" "text/x-java"]
     ["c#" "text/x-csharp"]
     ["c" "text/x-csrc"]
     ["c++" "text/x-c++src"]
     ["objective-c" "text/x-objectivec"]
     ["kotlin" "text/x-kotlin"]
     ["sql" "sql"]
     ["haskell" "haskell"]
     ["scala" "text/x-scala"]
     ["common lisp" "commonlisp"]
     ["julia" "julia"]
     ["sparql" "sparql"]
     ["turtle" "text/turtle"]])


(def default-code-mode
  {:names ["javascript" "js"]
   :extension #(javascript)})

(def code-modes
  [default-code-mode
   {:names ["clojure" "clojurescript" "clj" "cljs"]
    :extension nil}
   {:names ["html"]
    :extension nil}
   #_{:names ["lua"]
      :extension (. StreamLanguage parser lua)}])


(defn lang-chooser [{:as props
                     :keys [on-item-select read-only?]}
                    current-mode]
  (r/with-let [*query-text (r/atom "")]
    [blueprint/select-wrapper
     {:items (filterv (fn [code-mode]
                        (and (not= code-mode current-mode)
                             (some
                              #(str/includes? % (str/lower-case @*query-text))
                              (map str/lower-case (:names code-mode)))))
                      code-modes)
      :item-renderer (fn [{:keys [names]} props]
                       [blueprint/menu-item
                        {:text (first names)
                         :key (first names)
                         :on-click (:handleClick props)
                         :class-name "bp3-small"}])
      :on-query-change #(reset! *query-text %1)
      :query @*query-text
      :filterable true
      :popover-props {:minimal true}
      :on-item-select #(do (on-item-select (-> % :names first))
                           (reset! *query-text "")) ;; dispatch event to change language
      :disabled read-only?}

     [blueprint/button
      {:minimal true
       :class-name "bp3-small"
       :right-icon "caret-down"
       :text (-> current-mode :names first)
       :disabled read-only?}]]))

(defn cm6-editor [{:as props
                   :keys [default-value
                          on-blur
                          on-change]}
                  {:as cm-opts
                   :keys [lineNumbers?
                          code-mode]}]
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
                    (when-not (false? lineNumbers?) (lineNumbers))
                    (foldGutter)
                    (javascript)
                    (.. EditorView -updateListener (of #(on-change (.. % -state -doc toString))))]
     *editor-view (r/atom nil)
     *editor-string (r/track #(.. @*editor-view -state -doc toString))
     mount! (fn [el]
              (when el
                (reset! *editor-view
                        (new EditorView
                             (j/obj :state (.create EditorState #js{:extensions extensions})
                                    :parent el)))))]
    [:div
     {:ref mount!
      :on-blur #(on-blur @*editor-string)}]
    (finally (j/call @*editor-view :destroy))))

;; this will be defcomp
(defn code-editor [state s _indexes]
  (let [first-line   (first (str/split s #"\n"))
        defined-mode (find-some #(= (-> % :names first) first-line) code-modes)
        code-mode    (or defined-mode default-code-mode)
        code-string  (if defined-mode
                       (subs s (inc (count first-line)))
                       s)
        *code-string (r/atom code-string)]
    [:div.rm-code-block

     [cm6-editor
      {:default-value s
       :code-mode code-mode
       :on-change #(reset! *code-string %)
       :on-blur #(reset! state (str (-> code-mode :names first) "\n" %))} ;; dispatch function here
      {}]

     [:div.bg-gray-100.text-xs.flex.justify-end.text-gray-500 ;; convert to plain css?
      [lang-chooser
       {:on-item-select #(reset! state (str % "\n" @*code-string))
        :read-only? false}
       code-mode]]]))

(defn app []
  (r/with-let [state (r/atom "")]
    [:div.container.max-w-screen-md.mx-auto
     [:div.w-96.h-48.m-auto.mt-24
      [:div.rounded-md.border.shadow-lg.bg-white
       [code-editor state @state nil]]]
     [:div
      "Actual stored string: "
      [:pre.mt-2.whitespace-pre-wrap @state]]]))