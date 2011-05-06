(ns quotes.views
 (:require [clj-time.core :as time] [clj-time.format :as format] [clj-time.coerce :as coerce] [net.cgrand.enlive-html :as html]))

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

(html/defsnippet quotes-browse-model "views/quotes-browse.html" [[:#quotes-content]] [quotes]
  [:li.quote] (html/clone-for
    [{:keys [body timestamp id flagged]} quotes]
    [:h3] (html/set-attr :id (str "quote-header-" id))
    [:blockquote.quote-body :p] (html/content body)
    [:span.quote-id] (html/content (str "#" id))
    [:a.quote-permalink] (html/set-attr :href (str "/quotes/" id))
    [:a.quote-rating-up] (html/do-> (html/remove-class "casted-vote") (html/set-attr :href (str "#") :id (str "quote-rating-up-" id)))
    [:a.quote-rating-down] (html/do-> (html/remove-class "casted-vote")(html/set-attr :href (str "#") :id (str "quote-rating-down-" id)))
    [:span.quote-rating] (html/do-> (html/set-attr :id (str "quote-rating-" id)) (html/content "+1"))
    [:span#quote-vote-count-ID] (html/do-> (html/set-attr :id (str "quote-vote-count-" id)) (html/content "0"))
    [:span.quote-flagged] #(when (true? flagged) %)
    [:a.quote-report] #(when (not flagged) ((html/set-attr :id (str "quote-report-" id)) %))
        ;((html/content "[REPORT]"))); (html/set-attr :id (str "quote-report-" id) :href "#"))))
    [:span.quote-date] (html/content (format/unparse (format/formatters :rfc822) (coerce/from-long timestamp)))
    ))

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
  [success]
  (if (true? success)
    (base (assoc *context* :content {:title "Quote Submitted" :text "Thank you for submitting a quote to our database. A site administrator will review it shortly. If it gets approved, it will appear on this web site. Fingers crossed!"}) simple-message-content)
    (base (assoc *context* :content { :title "Submission Failed" :text "There was an error. Sorry :("}) simple-message-content)))

(defn browse-quotes-html [quotes]
  (base (assoc *context* :content (quotes-browse-model quotes)) quotes-browse-content))
