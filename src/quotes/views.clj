(ns quotes.views
 (:require [net.cgrand.enlive-html :as html]))

(def *context*
  {:header {:title "VT Bash" :href "/"
   :nav [{:text "Browse" :href "/quotes"}
         {:text "Submit" :href "/quotes/submit"}]}})


(def *nav-sel* [[:.nav-item]])

(html/defsnippet nav-model "views/index.html" *nav-sel*
  [{:keys [text href]}]
  [:a] (html/do->
        (html/content text)
        (html/set-attr :href href)))

(html/defsnippet header-model "views/index.html" [[:div#header]]
  [{:keys [title href nav]}]
  [:h1#title] (html/content {:tag :a, :attrs {:href href}, :content title})
  [:ul#navigation] (html/content (map nav-model nav)))

(html/defsnippet quote-form-model "views/quote-form.html" [[:#submit-form]] [])

(html/defsnippet quotes-browse-model "views/quotes-browse.html" [[:#quotes-content]] [])

(html/defsnippet simple-message-model (html/html-snippet "<div id=\"message\"><h2></h2><p></p></div>") [:#message]
  [{:keys [title text]}]
  [:h2] (html/content title)
  [:p]  (html/content text))

(defn index-content [content]
  (html/content content))

(defn quote-form-content [content]
 [:#submit-form] (html/content content))

(defn simple-message-content [content]
  (html/content (simple-message-model content)))

(defn quotes-browse-content [content]
  [:#quotes-content] (html/content content))

(html/deftemplate base "views/index.html" [{:keys [header content]} content-model]
  [:#header] (html/substitute (header-model header))
  [:#content] (content-model content)
)

(defn index-html
  ([] (base (assoc *context* :content "Welcome!") index-content)))

(defn quote-form-html
  ([] (base (assoc *context* :content (quote-form-model)) quote-form-content)))

(defn quote-submitted
  ([] (base (assoc *context* :content {:title "Quote Submitted" :text "Thank you for submitting a quote to our database. A site administrator will review it shortly. If it gets approved, it will appear on this web site. Fingers crossed!"}) simple-message-content)))

(defn browse-quotes-html
  ([] (base (assoc *context* :content (quotes-browse-model)) quotes-browse-content)))
